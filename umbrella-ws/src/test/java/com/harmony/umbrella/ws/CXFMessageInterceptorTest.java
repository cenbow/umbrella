package com.harmony.umbrella.ws;

import java.util.Set;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;

import com.harmony.umbrella.ws.cxf.interceptor.MessageInInterceptor;
import com.harmony.umbrella.ws.cxf.interceptor.MessageOutInterceptor;
import com.harmony.umbrella.ws.jaxws.JaxWsProxyBuilder;
import com.harmony.umbrella.ws.jaxws.JaxWsServerBuilder;
import com.harmony.umbrella.ws.services.HelloService;
import com.harmony.umbrella.ws.services.HelloWebService;

/**
 * @author wuxii@foxmail.com
 */
public class CXFMessageInterceptorTest {

    private static final String address = "http://localhost:8081/hello";

    public static void main(String[] args) {
        JaxWsServerBuilder.create().publish(HelloWebService.class, address, new FactoryConfig<JaxWsServerFactoryBean>() {

            @Override
            public void config(JaxWsServerFactoryBean factoryBean) {
                factoryBean.getInInterceptors().add(new MessageInInterceptor() {

                    @Override
                    public void handleMessage(Message message) throws Fault {

                        Set<String> keySet = message.keySet();
                        for (String string : keySet) {
                            Object obj = message.get(string);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server in message: key=" + string + ", value=" + obj + ", type=" + type);
                        }

                        System.out.println();
                        Set<Class<?>> contentFormats = message.getContentFormats();
                        for (Class<?> class1 : contentFormats) {
                            Object obj = message.getContent(class1);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server in content: key=" + class1.getName() + ", value=" + obj + ", type=" + type);
                        }

                        System.out.println();
                        Set<String> contextualPropertyKeys = message.getContextualPropertyKeys();
                        for (String string : contextualPropertyKeys) {
                            Object obj = message.getContextualProperty(string);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server in contextual: key=" + string + ", value=" + obj + ", type=" + type);
                        }
                    }

                });

                factoryBean.getOutInterceptors().add(new MessageOutInterceptor() {
                    @Override
                    public void handleMessage(Message message) throws Fault {

                        Set<String> keySet = message.keySet();
                        for (String string : keySet) {
                            Object obj = message.get(string);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server out message: key=" + string + ", value=" + obj + ", type=" + type);
                        }

                        System.out.println();
                        Set<Class<?>> contentFormats = message.getContentFormats();
                        for (Class<?> class1 : contentFormats) {
                            Object obj = message.getContent(class1);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server out content: key=" + class1.getName() + ", value=" + obj + ", type=" + type);
                        }

                        System.out.println();
                        Set<String> contextualPropertyKeys = message.getContextualPropertyKeys();
                        for (String string : contextualPropertyKeys) {
                            Object obj = message.getContextualProperty(string);
                            String type = obj != null ? obj.getClass().getName() : "null";
                            System.out.println("server out contextual: key=" + string + ", value=" + obj + ", type=" + type);
                        }
                    }
                });

            }

        });

        JaxWsProxyBuilder builder = JaxWsProxyBuilder.create();

        builder.getInInterceptors().add(new MessageInInterceptor() {

            @Override
            public void handleMessage(Message message) throws Fault {
                Set<String> keySet = message.keySet();
                for (String string : keySet) {
                    Object obj = message.get(string);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy in message: key=" + string + ", value=" + obj + ", type=" + type);
                }

                System.out.println();
                Set<Class<?>> contentFormats = message.getContentFormats();
                for (Class<?> class1 : contentFormats) {
                    Object obj = message.getContent(class1);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy in content: key=" + class1.getName() + ", value=" + obj + ", type=" + type);
                }

                System.out.println();
                Set<String> contextualPropertyKeys = message.getContextualPropertyKeys();
                for (String string : contextualPropertyKeys) {
                    Object obj = message.getContextualProperty(string);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy in contextual: key=" + string + ", value=" + obj + ", type=" + type);
                }
            }

        });

        builder.getOutInterceptors().add(new MessageOutInterceptor() {
            @Override
            public void handleMessage(Message message) throws Fault {
                Set<String> keySet = message.keySet();
                for (String string : keySet) {
                    Object obj = message.get(string);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy out message: key=" + string + ", value=" + obj + ", type=" + type);
                }

                System.out.println();
                Set<Class<?>> contentFormats = message.getContentFormats();
                for (Class<?> class1 : contentFormats) {
                    Object obj = message.getContent(class1);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy out content: key=" + class1.getName() + ", value=" + obj + ", type=" + type);
                }

                System.out.println();
                Set<String> contextualPropertyKeys = message.getContextualPropertyKeys();
                for (String string : contextualPropertyKeys) {
                    Object obj = message.getContextualProperty(string);
                    String type = obj != null ? obj.getClass().getName() : "null";
                    System.out.println("proxy out contextual: key=" + string + ", value=" + obj + ", type=" + type);
                }
            }
        });

        HelloService service = builder.build(HelloService.class, address);

        service.sayHi("wuxii");

        System.exit(0);

    }

}
