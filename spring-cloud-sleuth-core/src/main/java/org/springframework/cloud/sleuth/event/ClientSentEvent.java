/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.sleuth.event;

import org.springframework.cloud.sleuth.Span;

/**
 * <b>cs</b> - Client Sent. The client has made a request (a client can be e.g.
 * {@link org.springframework.web.client.RestTemplate}. This annotation depicts
 * the start of the span.
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class ClientSentEvent extends SpanContainingEvent {

	public ClientSentEvent(Object source, Span span) {
		super(source, span);
	}

}