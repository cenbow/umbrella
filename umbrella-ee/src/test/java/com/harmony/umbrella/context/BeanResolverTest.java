/*
 * Copyright 2002-2015 the original author or authors.
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
package com.harmony.umbrella.context;

import com.harmony.umbrella.context.bean.JeeSessionLocal;
import com.harmony.umbrella.context.ee.AbstractBeanResolver;
import com.harmony.umbrella.context.ee.BeanDefinition;

/**
 * @author wuxii@foxmail.com
 */
public class BeanResolverTest {

    public static void main(String[] args) {
        AbstractBeanResolver resolver = new AbstractBeanResolver();
        resolver.getBeanSeparators().add("#");
        resolver.getBeanSuffixs().add("Bean");
        resolver.getLocalSuffixs().add("Local");
        resolver.getRemoteSuffixs().add("Remote");
        String[] names = resolver.guessNames(new BeanDefinition(JeeSessionLocal.class), null);
        for (String jndi : names) {
            System.out.println(jndi);
        }
    }

}