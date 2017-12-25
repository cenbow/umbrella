package com.harmony.umbrella.message.creator;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * @author wuxii@foxmail.com
 */
public class ObjectMessageCreator extends AbstractMessageCreator<ObjectMessage> {

    private static final long serialVersionUID = -4357148380966416521L;
    private Serializable object;

    public ObjectMessageCreator(Serializable object) {
        this.object = object;
    }

    @Override
    protected void doMapping(ObjectMessage message) throws JMSException {
        message.setObject(object);
    }

    @Override
    protected ObjectMessage createMessage(Session session) throws JMSException {
        return session.createObjectMessage();
    }

}