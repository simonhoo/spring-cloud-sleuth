/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.instrument.async;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Executor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

/**
 * Bean post processor that wraps a call to an {@link Executor} either in a
 * JDK or CGLIB proxy. Depending on whether the implementation has a final
 * method or is final.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.4
 */
class ExecutorBeanPostProcessor implements BeanPostProcessor {

	private final BeanFactory beanFactory;

	ExecutorBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof Executor && !(bean instanceof ThreadPoolTaskExecutor)) {
			Method execute = ReflectionUtils.findMethod(bean.getClass(), "execute", Runnable.class);
			boolean methodFinal = Modifier.isFinal(execute.getModifiers());
			boolean classFinal = Modifier.isFinal(bean.getClass().getModifiers());
			boolean cglibProxy = !methodFinal && !classFinal;
			Executor executor = (Executor) bean;
			ProxyFactoryBean factory = new ProxyFactoryBean();
			factory.setProxyTargetClass(cglibProxy);
			factory.addAdvice(new ExecutorMethodInterceptor(executor, this.beanFactory));
			factory.setTarget(bean);
			return factory.getObject();
		}
		return bean;
	}
}

class ExecutorMethodInterceptor implements MethodInterceptor {

	private final Executor delegate;
	private final BeanFactory beanFactory;

	ExecutorMethodInterceptor(Executor delegate, BeanFactory beanFactory) {
		this.delegate = delegate;
		this.beanFactory = beanFactory;
	}

	@Override public Object invoke(MethodInvocation invocation)
			throws Throwable {
		LazyTraceExecutor executor = new LazyTraceExecutor(this.beanFactory, this.delegate);
		Method methodOnTracedBean = getMethod(invocation, executor);
		if (methodOnTracedBean != null) {
			return methodOnTracedBean.invoke(executor, invocation.getArguments());
		}
		return invocation.proceed();
	}

	private Method getMethod(MethodInvocation invocation, Object object) {
		Method method = invocation.getMethod();
		return ReflectionUtils
				.findMethod(object.getClass(), method.getName(), method.getParameterTypes());
	}
}
