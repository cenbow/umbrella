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
package com.harmony.umbrella.message.jms;

import java.io.Serializable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;

import com.harmony.umbrella.message.Message;
import com.harmony.umbrella.message.MessageException;

/**
 * @author wuxii@foxmail.com
 */
public abstract class AbstractJmsMessageConsumer implements JmsMessageConsumer {

    @Override
    public Message consome() throws MessageException {
        return consome(createJmsConfig());
    }

    @Override
    public Message consome(JmsConfig config) throws MessageException {
        return consome(config, DEFAULT_RECEIVE_TIMEOUT);
    }

    @Override
    public Message consome(JmsConfig config, long timeout) throws MessageException {
        try {
            javax.jms.Message jmsMessage = consmeJmsMessage(config, timeout);
            if (jmsMessage == null) {
                return null;
            }
            if (jmsMessage instanceof ObjectMessage) {
                Serializable message = ((ObjectMessage) jmsMessage).getObject();
                if (message instanceof Message) {
                    // 唯一正确的消息
                    return (Message) message;
                }
                throw new MessageException("message is not " + Message.class.getName() + " instance");
            }
            throw new MessageException("message is not " + ObjectMessage.class.getName() + " instance");
        } catch (JMSException e) {
            throw new MessageException(e);
        }
    }

    @Override
    public javax.jms.Message consmeJmsMessage(JmsConfig config, long timeout) throws JMSException {
        try {
            config.start();
            MessageConsumer consumer = config.getMessageConsumer();
            return timeout < 0 ? consumer.receiveNoWait() : consumer.receive(timeout);
        } finally {
            config.stop();
        }
    }

    protected JmsConfig createJmsConfig() {
        return new SimpleJmsConfig(getConnectionFactory(), getDestination());
    }

    /**
     * JMS连接工厂
     * 
     * @return
     */
    protected abstract ConnectionFactory getConnectionFactory();

    /**
     * JMS的目的地
     * 
     * @return
     */
    protected abstract Destination getDestination();

}
