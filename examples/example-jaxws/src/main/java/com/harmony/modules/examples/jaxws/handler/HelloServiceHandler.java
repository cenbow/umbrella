/*
 * Copyright 2002-2014 the original author or authors.
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
package com.harmony.modules.examples.jaxws.handler;

import com.harmony.modules.examples.jaxws.HelloService;
import com.harmony.modules.jaxws.Handler;
import com.harmony.modules.jaxws.Handler.HandleMethod;
import com.harmony.modules.jaxws.Phase;

/**
 * @author wuxii
 */
@Handler(HelloService.class)
public class HelloServiceHandler {

	@HandleMethod(phase = Phase.PRE_INVOKE)
	public boolean sayHi(String name) {
		System.err.println("hello service pre invoke name " + name);
		return true;
	}

	@HandleMethod(phase = Phase.POST_INVOKE)
	public void sayHi(String result, String name) {
		System.err.println("hello service post invok result " + result);
	}

}