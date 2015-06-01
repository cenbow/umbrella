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
package com.harmony.umbrella.jaxws.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.harmony.umbrella.jaxws.JaxWsContext;
import com.harmony.umbrella.util.ClassUtils;
import com.harmony.umbrella.util.StringUtils;

/**
 * @author wuxii@foxmail.com
 */
public class SimpleJaxWsContext implements JaxWsContext, Serializable {

    private static final long serialVersionUID = -7702092080209756058L;
    private Map<String, Object> contextMap = new HashMap<String, Object>();
    private Class<?> serviceInterface;
    private String methodName;
    private Object[] parameters;
    private String address;
    private String username;
    private String password;
    private long connectionTimeout = -1;
    private long receiveTimeout = -1;

    public SimpleJaxWsContext() {
    }

    public SimpleJaxWsContext(Class<?> serviceInterface, String methodName, Object[] parameters) {
        this.serviceInterface = serviceInterface;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    @Override
    public Class<?> getServiceInterface() {
        return this.serviceInterface;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void put(String name, String value) {
        contextMap.put(name, value);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Object get(String name) {
        return contextMap.get(name);
    }

    @Override
    public boolean contains(String name) {
        return contextMap.containsKey(name);
    }

    public void putAll(Map<String, Object> m) {
        this.contextMap.putAll(m);
    }

    @Override
    public Method getMethod() throws NoSuchMethodException {
        Class<?>[] types = ClassUtils.toTypeArray(parameters);
        try {
            return serviceInterface.getMethod(methodName, types);
        } catch (NoSuchMethodException e) {
            for (Method method : serviceInterface.getMethods()) {
                if (method.getName().equals(methodName)) {
                    Class<?>[] pattern = method.getParameterTypes();
                    if (ClassUtils.typeEquals(pattern, types)) {
                        return method;
                    }
                }
            }
            throw e;
        }
    }

    @Override
    public Map<String, Object> getContextMap() {
        return Collections.unmodifiableMap(contextMap);
    }

    @Override
    public void put(String key, Object value) {
        contextMap.put(key, value);
    }

    @Override
    public Enumeration<String> getContextNames() {
        return new Enumeration<String>() {
            Iterator<String> it = contextMap.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public String nextElement() {
                return it.next();
            }

        };
    }

    @Override
    public String getServiceName() {
        return serviceInterface.getName();
    }

    @Override
    public Class<?> getServiceClass() {
        return serviceInterface;
    }

    @Override
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + Arrays.hashCode(parameters);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((serviceInterface == null) ? 0 : serviceInterface.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleJaxWsContext other = (SimpleJaxWsContext) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (!Arrays.equals(parameters, other.parameters))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (serviceInterface == null) {
            if (other.serviceInterface != null)
                return false;
        } else if (!serviceInterface.equals(other.serviceInterface))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        
        String methodId = null;
        
        try {
            methodId = StringUtils.getMethodIdentifiteName(getMethod());
        } catch (NoSuchMethodException e) {
        }
        
        if(methodId == null){
            StringBuilder sb = new StringBuilder();
            sb.append(serviceInterface == null ? "unknow" : serviceInterface.getName())
              .append("#").append(methodName)
              .append("(");
            if (parameters != null) {
                for (Object param : parameters) {
                    sb.append(param != null ? param.getClass().getName() : "unknow").append(", ");
                }
                if (sb.lastIndexOf(", ") == sb.length() - 2) {
                    sb.delete(sb.length() - 2, sb.length());
                }
            }
            methodId = sb.append(")").toString();
        }
        
        StringBuilder result = new StringBuilder("{\n");
        result.append("  address  <-> ").append(address).append("\n")
              .append("  methodId <-> ").append(methodId).append("\n")
              .append("  username <-> ").append(username).append("\n")
              .append("  password <-> ").append(password).append("\n")
              .append("  ctimeout <-> ").append(connectionTimeout).append("\n")
              .append("  rtimeout <-> ").append(receiveTimeout).append("\n");
        result.append("}");
        
        return result.toString();
    }
    
}
