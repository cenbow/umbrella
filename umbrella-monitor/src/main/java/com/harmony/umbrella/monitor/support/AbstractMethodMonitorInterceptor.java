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
package com.harmony.umbrella.monitor.support;

import java.lang.reflect.Method;

import com.harmony.umbrella.monitor.AbstractMonitor;
import com.harmony.umbrella.monitor.MethodMonitor;
import com.harmony.umbrella.monitor.ResourceMatcher;
import com.harmony.umbrella.monitor.matcher.MethodExpressionMatcher;

/**
 * 基于拦截器的实现，默认来接{@link MethodMonitor#DEFAULT_METHOD_PATTERN} 表达式的方法.
 * <p>
 * 当然要连接器能拦截到方法
 * 
 * @param <IC>
 *            方法执行的上下文， 如JDK动态代理的
 *            <p>
 *            {@linkplain java.lang.reflect.InvocationHandler}中的参数一样组成的反射上下文
 *            <p>
 *            {@linkplain javax.interceptor.InvocationContext}
 * @author wuxii@foxmail.com
 */
public abstract class AbstractMethodMonitorInterceptor<IC> extends AbstractMonitor<Method> implements MethodMonitor {

    /**
     * 资源模版匹配工具
     */
    private ResourceMatcher<Method> resourceMatcher;

    @Override
    public ResourceMatcher<Method> getResourceMatcher() {
        if (resourceMatcher == null) {
            resourceMatcher = new MethodExpressionMatcher();
        }
        return resourceMatcher;
    }

    /**
     * 监控的方法
     * 
     * @param ctx
     * @return
     * @see {@linkplain javax.interceptor.InvocationContext#getMethod()}
     */
    protected abstract Method getMethod(IC ctx);

    /**
     * 执行监控的目标方法
     * 
     * @param ctx
     * @return
     * @throws Exception
     * @see {@linkplain javax.interceptor.InvocationContext#proceed()}
     */
    protected abstract Object process(IC ctx) throws Exception;

    /**
     * 监控的入口(拦截器的入口)
     * 
     * @param ctx
     * @return
     * @throws Exception
     * @see {@linkplain javax.interceptor.AroundInvoke}
     */
    protected Object preMonitor(IC ctx) throws Exception {
        Method method = getMethod(ctx);
        return method == null || !isMonitored(method) ? process(ctx) : aroundMonitor(method, ctx);
    }

    protected abstract Object aroundMonitor(Method method, IC ctx) throws Exception;

}
