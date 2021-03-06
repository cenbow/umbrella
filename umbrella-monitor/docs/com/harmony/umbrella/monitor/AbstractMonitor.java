package com.harmony.umbrella.monitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;

import com.harmony.umbrella.monitor.annotation.InternalProperty;

import com.harmony.umbrella.monitor.annotation.Monitor;
import com.harmony.umbrella.monitor.annotation.Mode;
import com.harmony.umbrella.monitor.graph.AbstractGraph;
import com.harmony.umbrella.util.Assert;
import com.harmony.umbrella.util.ReflectionUtils;

/**
 * 监控基础抽象类
 *
 * @param <T>
 *            监控的资源类型，由子类指定
 * @author wuxii@foxmail.com
 */
public abstract class AbstractMonitor<T> implements com.harmony.umbrella.monitor.Monitor<T> {

    protected static final Log LOG = Logs.getLog(AbstractMonitor.class);

    /**
     * 受到监控的模版名单
     */
    protected final Set<String> patternList = new CopyOnWriteArraySet<String>();

    /**
     * 资源名单，综合{@link #getPolicy()}定性资源名单的意义
     */
    protected final Set<T> resourceList = new CopyOnWriteArraySet<T>();

    /**
     * 监控策略
     */
    protected MonitorPolicy policy = MonitorPolicy.WhiteList;

    /**
     * 创建资源匹配器
     * <p/>
     * 通过模版路径创建资源匹配工具
     *
     * @return 模版资源匹配工具
     */
    protected abstract ResourceMatcher<T> getResourceMatcher();

    @Override
    public MonitorPolicy getPolicy() {
        return policy;
    }

    @Override
    public void setPolicy(MonitorPolicy policy) {
        Assert.notNull(policy, "cant' t set null to policy");
        this.policy = policy;
    }

    public Set<T> getResources() {
        return resourceList;
    }

    @Override
    public Set<String> getPatterns() {
        return patternList;
    }

    @Override
    public boolean isMonitored(T resource) {
        switch (policy) {
        case Skip:
            return false;
        case All:
            return true;
        case WhiteList:
            // resource 表示白名单， 排除白名单中的所有
            if (resourceList.contains(resource)) {
                return false;
            }
        case BlockList:
            // resource 表示黑名单， 监控黑名单中所有
            if (resourceList.contains(resource)) {
                return true;
            }
        default:
            for (String pattern : patternList) {
                if (getResourceMatcher().match(pattern, resource)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removePattern(String pattern) {
        patternList.remove(pattern);
    }

    public void addPattern(String pattern) {
        Assert.notBlank(pattern, "can't monitor blank or null pattern");
        patternList.add(pattern);
    }

    public void removeResource(T resource) {
        resourceList.remove(resource);
    }

    public void addResource(T resource) {
        Assert.notNull(resource, "can't monitor null resource");
        resourceList.add(resource);
    }

    public void cleanAll() {
        patternList.clear();
        resourceList.clear();
    }

    /**
     * 根据注解信息设置监控视图对于的信息
     * <ul>
     * <li>{@linkplain Graph#getModule() model}监控的模块
     * <li>{@linkplain Graph#getOperator() operator}监控的操作
     * <li>{@linkplain Graph#getLevel() level}监控的级别
     * </ul>
     *
     * @param graph
     *            监控结果视图
     * @param monitor
     *            被监控对象的注解信息
     */
    protected void applyMonitorInformation(AbstractGraph graph, Monitor monitor) {
        // TODO apply monitor information
    }

    /**
     * 根据方法上的注解获取需要监控的内部信息属性
     *
     * @param target
     *            被监控的对象
     * @param method
     *            当前监控的方法
     * @param mode
     *            监控的环节
     * @return 被监控对象的内部属性
     */
    public Map<String, Object> attackMethodProperty(Object target, Method method, Mode mode) {
        InternalProperty[] properties = getMonitorProperty(method, InternalProperty.class);
        return attackProperty(target, properties, mode);
    }

    /**
     * 根据注解信息获取监控对象的信息
     *
     * @param target
     *            被监控对象
     * @param properties
     *            需要监控的内部属性注解
     * @param mode
     *            监控的环节
     * @return 监控的内部属性
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> attackProperty(Object target, InternalProperty[] properties, Mode mode) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (properties != null && properties.length > 0) {
            for (InternalProperty internalProperty : properties) {
                if (mode.inRange(internalProperty.mode())) {
                    Attacker attacker = getAttacker(internalProperty.attacker());
                    result.putAll(attacker.attack(target, internalProperty.names()));
                }
            }
        }
        return result;
    }

    /**
     * 获取method上的{@linkplain Monitor}注解，并获取注解内对于的属性(传入的propertyType)
     *
     * @param method
     *            监控的方法
     * @param propertyType
     *            要获取的属性类型
     * @return 对应propertyType的属性， if not have monitor annotation return null
     */
    @SuppressWarnings("unchecked")
    protected final <E extends Annotation> E[] getMonitorProperty(Method method, Class<E> propertyType) {
        Monitor ann = method.getAnnotation(Monitor.class);
        return (E[]) (ann == null ? null : propertyType == InternalProperty.class ? ann.internalProperties() : ann.httpProperties());
    }

    /**
     * 获取clazz上的{@linkplain Monitor}注解，并获取注解内对于的属性(传入的propertyType)
     *
     * @param clazz
     *            监控的类
     * @param propertyType
     *            需要获取的属性类型
     * @return 对应propertyType的属性， if not have monitor annotation return null
     */
    @SuppressWarnings("unchecked")
    protected final <E extends Annotation> E[] getMonitorProperty(Class<?> clazz, Class<E> propertyType) {
        Monitor ann = clazz.getAnnotation(Monitor.class);
        return (E[]) (ann == null ? null : propertyType == InternalProperty.class ? ann.internalProperties() : ann.httpProperties());
    }

    /**
     * 根据attacker class创建attacker
     *
     * @param attackerClass
     *            attacker class
     * @return attacker instance
     */
    @SuppressWarnings("rawtypes")
    protected Attacker<?> getAttacker(Class<? extends Attacker> attackerClass) {
        return ReflectionUtils.instantiateClass(attackerClass);
    }

}
