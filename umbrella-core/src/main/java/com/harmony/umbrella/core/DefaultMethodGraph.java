package com.harmony.umbrella.core;

import java.lang.reflect.Method;

import com.harmony.umbrella.util.StringUtils;

/**
 * @author wuxii@foxmail.com
 */
public class DefaultMethodGraph implements MethodGraph {

    protected final Method method;

    protected Object target;
    protected Object result;
    protected Object[] parameters;

    protected long requestTime = -1;
    protected long responseTime = -1;
    protected Throwable throwable;

    public DefaultMethodGraph(Method method) {
        this.method = method;
    }

    public DefaultMethodGraph(Object target, Method method, Object[] parameters) {
        this.method = method;
        this.parameters = parameters;
        this.target = target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Class<?> getTargetClass() {
        return target.getClass();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public long getRequestTime() {
        return requestTime;
    }

    @Override
    public long getResponseTime() {
        return responseTime;
    }

    @Override
    public long use() {
        return requestTime > 0 && responseTime > 0 ? responseTime - requestTime : -1;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public boolean isThrowable() {
        return throwable != null;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String getDescription() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\n");
        buffer.append("  method:").append(StringUtils.getMethodId(method)).append("\n");
        buffer.append("  requestTime:").append(requestTime).append("\n");//
        buffer.append("  use:").append(use()).append("\n");//
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                buffer.append("  parameters[").append(i).append("]").append(parameters[i]).append("\n");//
            }
        } else {
            buffer.append("  parameters:").append(parameters).append("\n");//
        }
        buffer.append("  result:").append(result).append("\n");//
        buffer.append("  throwable:").append(isThrowable()).append("\n");
        if (isThrowable()) {
            buffer.append("  exceptionMessage:").append(throwable).append("\n");
        }
        return buffer.append("}").toString();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}