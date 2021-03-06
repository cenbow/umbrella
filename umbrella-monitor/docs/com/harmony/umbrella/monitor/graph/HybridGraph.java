package com.harmony.umbrella.monitor.graph;

import java.lang.reflect.Method;

import com.harmony.umbrella.monitor.HttpGraph;
import com.harmony.umbrella.core.MethodGraph;
import com.harmony.umbrella.monitor.util.MonitorUtils;

/**
 * 混合视图, 既包括了Http部分信息, 也包括了方法拦截的信息
 * <p>
 * 适用场景如:Struts2 的拦截器, 既可以包括Http部分信息, 也可以包括拦截的方法信息
 * 
 * @author wuxii@foxmail.com
 */
public class HybridGraph extends AbstractGraph implements MethodGraph, HttpGraph {

    private static final long serialVersionUID = -5531854949385303033L;
    protected String httpMethod;
    protected String remoteAddr;
    protected String localAddr;
    protected String queryString;
    protected int status;
    protected transient Method method;
    protected transient Object target;

    public HybridGraph(Method method) {
        this(MonitorUtils.methodId(method));
    }

    public HybridGraph(String identifier) {
        super(identifier);
    }

    public HybridGraph(String identifier, Object target) {
        super(identifier);
        this.target = target;
    }

    public HybridGraph(Method method, Object target, Object[] arguments) {
        super(MonitorUtils.methodId(method));
        this.target = target;
        this.setMethodArgumets(arguments);
    }

    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Class<?> getTargetClass() {
        return target != null ? target.getClass() : null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Object[] getMethodArguments() {
        return (Object[]) arguments.get(METHOD_ARGUMENT);
    }

    public void setMethodArgumets(Object... arguments) {
        this.arguments.put(METHOD_ARGUMENT, arguments);
    }

    @Override
    public Object getMethodResult() {
        return result.get(METHOD_RESULT);
    }

    public void setMethodResult(Object result) {
        this.result.put(METHOD_RESULT, result);
    }

    public boolean hasMethodGraph() {
        return method != null;
    }

    public boolean hasHttpGraph() {
        return httpMethod != null;
    }

    @Override
    public String getDescription() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\n")//
                .append("  url:").append(identifier).append("\n");//
        if (method != null) {
            buffer.append("  method:").append(MonitorUtils.methodId(method)).append("\n");
        }
        buffer.append("  remoteAddr:").append(remoteAddr).append("\n")//
                .append("  httpMethod:").append(httpMethod).append("\n")//
                .append("  requestTime:").append(ndf.format(requestTime)).append("\n")//
                .append("  use:").append(use()).append("\n")//
                .append("  status:").append(status).append("\n")//
                .append("  arguments:").append(getJsonArguments()).append("\n")//
                .append("  result:").append(getJsonResult()).append("\n")//
                .append("  exception:").append(isException()).append("\n");
        if (isException()) {
            buffer.append("  exceptionMessage:").append(exception).append("\n");
        }
        return buffer.append("}").toString();
    }

}
