package com.harmony.umbrella.ws.cxf.interceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ListIterator;

import org.apache.cxf.common.util.ReflectionUtil;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;

import com.harmony.umbrella.context.ApplicationContext;
import com.harmony.umbrella.core.BeanFactory;
import com.harmony.umbrella.util.MethodUtils;
import com.harmony.umbrella.util.StringUtils;

/**
 * @author wuxii@foxmail.com
 */
public class BeanInjectInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Log log = Logs.getLog(BeanInjectInterceptor.class);
    private BeanFactory beanFactory;

    public BeanInjectInterceptor() {
        this(Phase.PREPARE_SEND_ENDING);
    }

    public BeanInjectInterceptor(String phase) {
        super(phase);
        this.beanFactory = ApplicationContext.getApplicationContext();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handleMessage(Message message) throws Fault {
        InterceptorChain chain = message.getInterceptorChain();
        ListIterator<Interceptor<? extends Message>> iterator = chain.getIterator();
        while (iterator.hasNext()) {
            Interceptor<? extends Message> interceptor = iterator.next();
            Class<? extends Interceptor> interceptorClass = interceptor.getClass();
            Field[] fields = ReflectionUtil.getDeclaredFields(interceptorClass);
            for (Field field : fields) {
                Inject ann = field.getAnnotation(Inject.class);
                Object bean = null;
                if (ann != null) {
                    try {
                        if (StringUtils.isNotBlank(ann.value())) {
                            bean = beanFactory.getBean(ann.value());
                        } else {
                            bean = beanFactory.getBean(field.getType());
                        }
                        try {
                            Method method = MethodUtils.findWriterMethod(interceptorClass, field);
                            method.invoke(interceptor, bean);
                        } catch (NoSuchMethodException e) {
                            ReflectionUtil.setAccessible(field);
                            field.set(interceptor, bean);
                        }
                    } catch (Exception e) {
                        log.warn(interceptor + " inject " + field.getType().getName() + " failed", e.toString());
                    }
                }
            }
        }
    }

    @Documented
    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Inject {

        String value() default "";

    }
}
