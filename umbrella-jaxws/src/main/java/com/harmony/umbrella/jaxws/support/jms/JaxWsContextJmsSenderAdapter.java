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
package com.harmony.umbrella.jaxws.support.jms;

import com.harmony.umbrella.jaxws.JaxWsContext;
import com.harmony.umbrella.jaxws.support.JaxWsContextSender;
import com.harmony.umbrella.message.AbstractJmsMessageSender;
import com.harmony.umbrella.message.Message;

/**
 * @author wuxii@foxmail.com
 */
public abstract class JaxWsContextJmsSenderAdapter extends AbstractJmsMessageSender implements JaxWsContextSender {

    @Override
    public boolean send(JaxWsContext context) {
        return send(createMessage(context));
    }

    /**
     * 将{@linkplain JaxWsContext}转化为可以传递到消息服务中的消息
     * 
     * @param context
     * @return
     */
    public abstract Message createMessage(JaxWsContext context);

    @Override
    public void open() {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean isClosed() {
        return false;
    }

}