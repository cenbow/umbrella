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
package com.harmony.umbrella.ws.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.harmony.umbrella.Constants;
import com.harmony.umbrella.context.ApplicationContext;
import com.harmony.umbrella.core.BeanFactory;
import com.harmony.umbrella.ws.Context;
import com.harmony.umbrella.ws.Proxy;
import com.harmony.umbrella.ws.SyncCallback;
import com.harmony.umbrella.ws.Syncable;
import com.harmony.umbrella.ws.WebServiceAbortException;
import com.harmony.umbrella.ws.util.CallbackFinder;

/**
 * 业务回调的扩展周期访问者
 * <p>
 * {@linkplain Syncable}, {@linkplain SyncCallback}主要扩展对象,
 * {@linkplain SyncableContextVisitor}负责加载类路径下的所有标注有{@linkplain Syncable}
 * 的类，如果标注了该注解的类是{@linkplain SyncCallback} 接口的实现类这认为对业务具有回调作用。将在执行周期中被回调周期访问者
 * {@linkplain SyncableContextVisitor}执行回调方法
 * 
 * @author wuxii@foxmail.com
 */
public class SyncableContextVisitor extends AbstractContextVisitor {

    /**
     * 用户扫描类路径下的{@linkplain Syncable}
     */
    private CallbackFinder callbackFinder;

    /**
     * 负责初始化回调的{@linkplain SyncCallback}
     */
    private BeanFactory beanFactory;

    public SyncableContextVisitor() {
        this(Constants.DEFAULT_PACKAGE);
    }

    public SyncableContextVisitor(String basePackage) {
        this.callbackFinder = new CallbackFinder(basePackage);
    }

    public SyncableContextVisitor(String basePackage, BeanFactory beanFactory) {
        this.callbackFinder = new CallbackFinder(basePackage);
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean visitBefore(Context context) throws WebServiceAbortException {
        Object syncObj = context.get(Proxy.SYNC_OBJECT);
        Map<String, Object> content = context.getContextMap();
        for (SyncCallback callback : getCallbacks(context)) {
            callback.forward(syncObj, content);
        }
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void visitCompletion(Object result, Context context) {
        Object syncObj = context.get(Proxy.SYNC_OBJECT);
        Map<String, Object> content = context.getContextMap();
        for (SyncCallback callback : getCallbacks(context)) {
            callback.success(syncObj, result, content);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void visitThrowing(Throwable throwable, Context context) {
        Object syncObj = context.get(Proxy.SYNC_OBJECT);
        Map<String, Object> content = context.getContextMap();
        for (SyncCallback callback : getCallbacks(context)) {
            callback.failed(syncObj, throwable, content);
        }
    }

    public void setCallbackFinder(CallbackFinder callbackFinder) {
        this.callbackFinder = callbackFinder;
    }

    /*
     * 延迟加载
     */
    protected BeanFactory getBeanFactory() {
        if (beanFactory == null) {
            beanFactory = ApplicationContext.getApplicationContext();
        }
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings("rawtypes")
    private List<SyncCallback> getCallbacks(Context context) {
        List<SyncCallback> result = new ArrayList<SyncCallback>();
        Class<SyncCallback>[] classes = callbackFinder.getCallbackClasses(context.getServiceClass(), context.getMethodName());
        if (classes != null && classes.length > 0) {
            BeanFactory beanFactory = getBeanFactory();
            for (Class<SyncCallback> callbackClass : classes) {
                result.add(beanFactory.getBean(callbackClass));
            }
        }
        return result;
    }
    
}