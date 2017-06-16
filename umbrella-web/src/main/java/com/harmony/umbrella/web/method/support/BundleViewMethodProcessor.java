package com.harmony.umbrella.web.method.support;

import static com.harmony.umbrella.web.method.annotation.BundleView.BehaviorType.*;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.serializer.SerializeFilter;
import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;
import com.harmony.umbrella.util.StringUtils;
import com.harmony.umbrella.web.method.annotation.BundleController;
import com.harmony.umbrella.web.method.annotation.BundleView;
import com.harmony.umbrella.web.method.annotation.BundleView.BehaviorType;
import com.harmony.umbrella.web.method.annotation.BundleView.PatternConverter;

/**
 * http request & response method processor
 * 
 * @author wuxii@foxmail.com
 * @see BundleView
 */
public class BundleViewMethodProcessor implements HandlerMethodReturnValueHandler, HandlerMethodArgumentResolver {

    private static final Log log = Logs.getLog(BundleViewMethodProcessor.class);

    public BundleViewMethodProcessor() {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(ViewFragment.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        final ViewFragment viewFragment = getOrCreateViewFragment(mavContainer, true);
        fillViewFragment(parameter, viewFragment);
        return viewFragment;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), BundleView.class) //
                || returnType.hasMethodAnnotation(BundleView.class));
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws Exception {
        mavContainer.setRequestHandled(true);
        boolean viewFragmentCreated = mavContainer.containsAttribute(ViewFragment.VIEW_FRAGMENT);
        ViewFragment viewFragment = getOrCreateViewFragment(mavContainer, false);
        if (!viewFragmentCreated) {
            fillViewFragment(returnType, viewFragment);
        }
        viewFragment.render(returnValue, webRequest.getNativeResponse(HttpServletResponse.class));
    }

    protected final ViewFragment getOrCreateViewFragment(ModelAndViewContainer mavContainer, boolean addAttribute) {
        Object o = mavContainer.getModel().get(ViewFragment.VIEW_FRAGMENT);
        if (o != null && !(o instanceof ViewFragment)) {
            throw new IllegalArgumentException("model attribute viewFragment not instance " + ViewFragment.class);
        }
        final ViewFragment viewFragment = (o == null) ? new ViewFragment() : (ViewFragment) o;
        if (addAttribute) {
            mavContainer.addAttribute(ViewFragment.VIEW_FRAGMENT, viewFragment);
        }
        return viewFragment;
    }

    /**
     * 根据方法上注有的{@code BundleView}注解来配置默认的{@code ViewFragment}
     * 
     * @param parameter
     *            方法参数
     * @param viewFragment
     *            viewFragment
     * @throws Exception
     *             can't create PatternConverter
     */
    protected void fillViewFragment(MethodParameter parameter, ViewFragment viewFragment) throws Exception {
        BundleView ann = parameter.getMethodAnnotation(BundleView.class);
        if (ann == null) {
            ann = BundleController.class.getAnnotation(BundleView.class);
        }
        if (ann == null) {
            log.info("{} no default bundle view", parameter);
            return;
        }
        Class<?> returnType = parameter.getMethod().getReturnType();
        final BehaviorType behavior;
        if (AUTO.equals(ann.behavior())) {
            behavior = BehaviorType.convert(returnType);
        } else {
            behavior = ann.behavior();
        }
        if (!NONE.equals(behavior)) {
            // apply default behavior
            behavior.apply(viewFragment);
        }

        viewFragment.features(ann.features())//
                .wrappage(ann.wrappage())//
                .fetchLazy(ann.fetchLazy())//
                .safeFetch(ann.safeFetch());//

        if (StringUtils.isNotBlank(ann.encoding())) {
            viewFragment.encoding(ann.encoding());
        }

        PatternConverter converter = behavior;
        if (!PatternConverter.class.equals(ann.converter())) {
            converter = ann.converter().newInstance();
        }

        if (ann.excludes().length > 0) {
            viewFragment.excludes(converter, ann.excludes());
        } else if (ann.includes().length > 0) {
            viewFragment.includes(converter, ann.includes());
        }

        if (StringUtils.isNotBlank(ann.contentType())) {
            viewFragment.contentType(ann.contentType());
        }

        Class<? extends SerializeFilter>[] filtersClasses = ann.filters();
        for (Class<? extends SerializeFilter> c : filtersClasses) {
            viewFragment.filters(c.newInstance());
        }
    }

}
