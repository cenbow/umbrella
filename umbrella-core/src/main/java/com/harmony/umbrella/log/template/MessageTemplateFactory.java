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
package com.harmony.umbrella.log.template;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import com.harmony.umbrella.log.HttpTemplate;
import com.harmony.umbrella.log.Template;
import com.harmony.umbrella.log.TemplateFactory;
import com.harmony.umbrella.log.annotation.Logging;

/**
 * @author wuxii@foxmail.com
 */
public class MessageTemplateFactory implements TemplateFactory {

    @Override
    public Template createTemplate(Logging logAnnotation) {
        if (logAnnotation == null) {
            throw new IllegalArgumentException("@Log annotation cannot be null");
        }
        return new MessageTemplate(logAnnotation);
    }

    public Template createTemplate(Method method) {
        Logging logAnnotation = method.getAnnotation(Logging.class);
        if (logAnnotation == null) {
            throw new IllegalArgumentException(method + " not have @Log annotation ");
        }
        return createTemplate(logAnnotation);
    }

    @Override
    public HttpTemplate createHttpTemplate(Logging logAnnotation, HttpServletRequest request) {
        if (logAnnotation == null) {
            throw new IllegalArgumentException("@Log annotation cannot be null");
        }
        return new HttpMessageTemplate(logAnnotation, request);
    }

    public HttpTemplate createHttpTemplate(Method method, HttpServletRequest request) {
        Logging logAnnotation = method.getAnnotation(Logging.class);
        if (logAnnotation == null) {
            throw new IllegalArgumentException(method + " not have @Log annotation ");
        }
        return createHttpTemplate(logAnnotation, request);
    }

}
