package com.harmony.umbrella.json.serializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.harmony.umbrella.core.Member;
import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;
import com.harmony.umbrella.util.MemberUtils;

/**
 * @author wuxii@foxmail.com
 */
public class LazyAttributeFilter extends MemberPropertyFilter {
    /**
     * 需要被懒加载处理的注解
     */
    public static final List<Class<? extends Annotation>> LAZY_ANNOTATION_CLASSES;

    static {
        List<Class<? extends Annotation>> temp = new ArrayList<>();
        temp.add(ManyToOne.class);
        temp.add(ManyToMany.class);
        temp.add(OneToMany.class);
        temp.add(OneToOne.class);
        LAZY_ANNOTATION_CLASSES = Collections.unmodifiableList(temp);
    }

    private static final Log log = Logs.getLog(LazyAttributeFilter.class);

    /**
     * 是否尝试通过get方法去加载lazy的属性
     */
    private boolean tryFetch;
    /**
     * 需要被过滤的lazy注解
     */
    private final Set<Class<? extends Annotation>> anns = new HashSet<Class<? extends Annotation>>();

    public LazyAttributeFilter(Class<? extends Annotation>... anns) {
        this(false, anns);
    }

    public LazyAttributeFilter(boolean tryFetch, Class<? extends Annotation>... anns) {
        this(tryFetch, Arrays.asList(anns));
    }

    public LazyAttributeFilter(boolean tryFetch, Collection<Class<? extends Annotation>> anns) {
        super(true);
        this.tryFetch = tryFetch;
        this.addFilterAnnotationClass(anns);
    }

    @Override
    protected boolean accept(Member member, Object target) {
        FetchType fetchType = getFetchType(member);
        return fetchType == null || FetchType.EAGER.equals(fetchType) || (tryFetch && tryFetch(member, target));
    }

    public boolean tryFetch(Member member, Object object) {
        try {
            Object v = member.get(object);
            if (v == null) {
                return true;
            } else if (v instanceof Collection) {
                ((Collection) v).size();
            } else {
                return tryFirstReadMethod(member.getType(), v);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean tryFirstReadMethod(Class<?> clazz, Object object) throws Exception {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (!ReflectionUtils.isObjectMethod(method) //
                    && Modifier.isPublic(method.getModifiers())//
                    && !Modifier.isStatic(method.getModifiers())//
                    && MemberUtils.isReadMethod(method)) {
                ReflectionUtils.invokeMethod(method, object);
                return true;
            }
        }
        return false;
    }

    public FetchType getFetchType(Member member) {
        Annotation ann = null;
        for (Class<? extends Annotation> annCls : anns) {
            ann = member.getAnnotation(annCls);
            if (ann != null) {
                break;
            }
        }
        return ann == null ? null : (FetchType) AnnotationUtils.getValue(ann, "fetch");
    }

    public void addFilterAnnotationClass(Iterable<Class<? extends Annotation>> annCls) {
        for (Class<? extends Annotation> cls : annCls) {
            try {
                if (cls.getMethod("fetch") != null) {
                    this.anns.add(cls);
                }
            } catch (Exception e) {
                log.error("{} annotation not have fetch attribute", cls, e);
            }
        }
    }

}
