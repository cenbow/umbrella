/*
 * Copyright 2002-2014 t
he original author or authors.
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
package com.harmony.umbrella.monitor.support;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import com.harmony.umbrella.monitor.MethodMonitor;

/**
 * @author wuxii@foxmail.com
 */
public class MethodMatcherTest {

	@Test
	public void test() throws Exception {
		MethodExpressionMatcher matcher = new MethodExpressionMatcher(MethodMonitor.DEFAULT_METHOD_PATTERN);
		Method method1 = Object.class.getDeclaredMethod("toString");
		assertFalse(matcher.matches(method1));
		Method method2 = getClass().getMethod("test");
		assertTrue(matcher.matches(method2));
	}

}