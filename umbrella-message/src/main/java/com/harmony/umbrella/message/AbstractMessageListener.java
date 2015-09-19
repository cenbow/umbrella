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
package com.harmony.umbrella.message;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuxii@foxmail.com
 */
public abstract class AbstractMessageListener implements MessageListener {

    protected final static Logger LOG = LoggerFactory.getLogger(AbstractMessageListener.class);

    protected final List<Class<? extends MessageResolver>> resolverClasses = new ArrayList<Class<? extends MessageResolver>>();

    public AbstractMessageListener() {
    }

    public AbstractMessageListener(List<Class<? extends MessageResolver>> resolverClasses) {
        this.resolverClasses.addAll(resolverClasses);
    }

    protected abstract List<MessageResolver> getMessageResolvers();

    /**
     * 接受到的消息, 使用{@linkplain MessageResolver#support(Message)}判定当前有哪些是符合条件的
     * {@linkplain MessageResolver}. 再经由
     * {@linkplain MessageResolver#handle(Message)}处理该消息.
     * <p/>
     * <p>
     * 消息是可以被多个{@linkplain MessageResolver}按顺序处理的
     *
     * @see MessageResolver
     */
    @Override
    public void onMessage(Message message) {
        for (MessageResolver mr : getMessageResolvers()) {
            if (mr.support(message)) {
                LOG.debug("{}处理消息{}", mr, message);
                mr.handle(message);
            }
        }
    }

    /**
     * 动态增加{@linkplain MessageResolver}
     *
     * @param messageResolver
     * @return
     */
    public boolean addResolverClass(Class<? extends MessageResolver> resolverClass) {
        return resolverClasses.add(resolverClass);
    }

    /**
     * 动态删除{@linkplain MessageResolver}
     *
     * @param messageResolver
     * @return
     */
    public boolean removeResolverClass(Class<? extends MessageResolver> resolverClass) {
        return resolverClasses.remove(resolverClass);
    }

    public void removeAllResolverClass() {
        this.resolverClasses.clear();
    }

    @Override
    public void destroy() {
        this.removeAllResolverClass();
    }

}
