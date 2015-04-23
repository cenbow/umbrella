/*
 * Copyright 2013-2015 wuxii@foxmail.com.
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
package com.harmony.umbrella.context.ee;

/**
 * @author wuxii@foxmail.com
 */
public interface JndiNameFormat {

	String PROP_KEY_BEAN = "jndi.format.bean";
	String PROP_KEY_REMOTE = "jndi.format.remote";
	String PROP_KEY_LOCAL = "jndi.format.local";

	String SUFFIX_BEAN = "Bean";
	String SUFFIX_REMOTE = "Remote";
	String SUFFIX_LOCAL = "Local";

	String format(BeanDefinition beanDefinition);

}
