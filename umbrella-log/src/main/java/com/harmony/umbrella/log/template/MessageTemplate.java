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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.harmony.umbrella.log.Logs;
import com.harmony.umbrella.log.Message;
import com.harmony.umbrella.log.Template;
import com.harmony.umbrella.log.annotation.Log;
import com.harmony.umbrella.log.message.FormattedMessage;
import com.harmony.umbrella.log.message.SimpleMessage;

/**
 * @author wuxii@foxmail.com
 */
public class MessageTemplate implements Template {

    public static final String ARRAY_EXPRESSION_PATTERN = "\\w+\\[[0-9]+\\]";
    public static final String MAP_EXPRESSION_PATTERN = "\\w+\\[\\w+\\]";
    public static final String NUMBER_EXPRESSION_PATTERN = "[0-9]+";

    protected static final Pattern arrayExpressionPattern = Pattern.compile(ARRAY_EXPRESSION_PATTERN);
    protected static final Pattern mapExpressionPattern = Pattern.compile(MAP_EXPRESSION_PATTERN);
    protected static final Pattern numberExpressionPattern = Pattern.compile(NUMBER_EXPRESSION_PATTERN);

    private static final com.harmony.umbrella.log.Log log = Logs.getLog(MessageTemplate.class);

    protected final Log ann;
    protected final Method method;
    protected final String template;
    protected final String idExpression;

    // use get method replace
    private List<String> _templateExpressions;
    private String _placeholdTemplate;

    public MessageTemplate(Method method) {
        this.ann = method.getAnnotation(Log.class);
        if (ann == null) {
            throw new IllegalArgumentException("method not have @Log annotation");
        }
        if ("".equals(ann.message())) {
            throw new IllegalArgumentException("method not a template method, check @Log#message() property");
        }
        this.template = ann.message();
        this.idExpression = ann.id();
        this.method = method;
    }

    @Override
    public Message newMessage(Object target, Object result, Object[] params) {
        String[] expressions = getTemplateExpression();

        if (expressions.length == 0) {
            return new SimpleMessage(template);
        }

        List<Object> args = new ArrayList<Object>();

        for (String expression : expressions) {
            args.add(getExpressionValue(expression, target, result, params));
        }

        return new FormattedMessage(getPlaceholdTemplate(), args.toArray(new Object[args.size()]));
    }

    @Override
    public String getId(Object target, Object result, Object[] params) {
        if (hasIdExpression()) {
            Object value = getExpressionValue(idExpression, target, result, params);
            return String.valueOf(value);
        }
        return null;
    }

    /**
     * 根据表达式关键字获取对应的值
     */
    protected final Object getExpressionValue(final String expression, Object target, Object result, Object[] params) {
        if (expression == null || "".equals(expression)) {
            throw new IllegalArgumentException("expression is null or empty");
        }

        final String exp = clearExpression(expression);

        if ("target".equals(exp)) {
            return target;
        } else if ("result".equals(exp)) {
            return result;
        } else if ("id".equals(exp)) {
            return getId(target, result, params);
        } else if (exp.startsWith("target.")) {
            return getValue(exp, target);
        } else if (exp.startsWith("result.")) {
            return getValue(exp, result);
        } else if (exp.startsWith("id.")) {
            return getValue(exp, getId(target, result, params));
        } else if (isNumberExpression(exp)) {
            // 表达式为数字，由params参数中找出值
            int index = Integer.parseInt(exp);
            if (index >= params.length) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            return params[index];
        } else {
            int dotIndex = exp.indexOf(".");
            if (dotIndex > 0) {
                String firstExpression = exp.substring(0, dotIndex);
                if (isNumberExpression(firstExpression)) {
                    int index = Integer.parseInt(firstExpression);
                    if (index >= params.length) {
                        throw new ArrayIndexOutOfBoundsException(index);
                    }
                    return getValue(exp.substring(dotIndex + 1), params[index]);
                }
            }
        }

        return getUnrecognizedExpressionValue(expression, target, result, params);
    }

    public final String clearExpression(String expression) {
        if (expression == null) {
            return null;
        }
        if (expression.startsWith("{") && expression.endsWith("}")) {
            return expression.substring(1, expression.length() - 1).trim();
        }
        return expression.trim();
    }

    protected Object getUnrecognizedExpressionValue(String expression, Object value, Object result, Object[] params) {
        return null;
    }

    /**
     * 一串表达式根据<code>.</code>分割为一个个单独的表达式，再根据各个表达式求值
     */
    protected Object getValue(String expression, Object obj) {
        Object target = obj;
        StringTokenizer st = new StringTokenizer(expression, ".");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            try {
                target = getSingleExpressionValue(token, target);
            } catch (Exception e) {
                throw new IllegalArgumentException("illegal expression " + expression, e);
            }
        }
        return target;
    }

    /**
     * 求单个表达式的值
     * @throws Exception 
     */
    @SuppressWarnings("rawtypes")
    protected Object getSingleExpressionValue(String expression, Object value) throws Exception {
        final int index = expression.indexOf("[");
        final String name = index > 0 ? expression.substring(0, index) : expression;

        Object result = accessValue(name, value);

        if (index > 0) {
            if (isArrayExpression(expression)) {
                return getValueInArrayOrCollection(getArrayExpressionIndex(expression), result);
            } else if (isMapExpression(expression) && result instanceof Map) {
                return ((Map) result).get(getMapExpressionKey(expression));
            }
            log.warn("expression {} exist '[' but result is not array or map", result);
        }

        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object getValueInArrayOrCollection(int expressionArrayIndex, Object result) {
        Class<?> resultClass = result.getClass();
        if (result instanceof Collection) {
            ArrayList list = new ArrayList((Collection) result);
            if (list.size() > expressionArrayIndex) {
                return list.get(expressionArrayIndex);
            }
            throw new ArrayIndexOutOfBoundsException(expressionArrayIndex);
        } else if (resultClass.isArray()) {
            // TODO 数组支持
        }
        throw new RuntimeException("unsupported array type " + resultClass.getName());
    }

    private Object accessValue(String name, Object value) throws Exception {
        Object result = null;
        Class<?> targetClass = value.getClass();
        try {
            Method method = targetClass.getDeclaredMethod(readMethodName(name));
            result = method.invoke(value);
        } catch (Exception e) {
            try {
                Field field = targetClass.getDeclaredField(name);
                field.setAccessible(true);
                result = field.get(value);
            } catch (Exception e1) {
                throw e;
            }
        }
        return result;
    }

    /**
     * 检测表达式是否是数组表达式
     */
    public boolean isArrayExpression(String expression) {
        return arrayExpressionPattern.matcher(expression).matches();
    }

    public boolean isMapExpression(String expression) {
        return mapExpressionPattern.matcher(expression).matches();
    }

    public boolean isNumberExpression(String expression) {
        return numberExpressionPattern.matcher(expression).matches();
    }

    protected final String getMapExpressionKey(String expression) {
        int left = expression.indexOf("[");
        int right = expression.indexOf("]");
        if (left > 0 && right > 0 && left < right) {
            return expression.substring(left + 1, right);
        }
        return null;
    }

    protected final int getArrayExpressionIndex(String expression) {
        int left = expression.indexOf("[");
        int right = expression.indexOf("]");
        if (left > 0 && right > 0 && left < right) {
            return Integer.parseInt(expression.substring(left + 1, right));
        }
        return -1;
    }

    /**
     * 截取表达式，截取完后的表达式包含了表达式的前后字符
     * <p>
     * <code>"数据{0.name}保存{result}" 对应的结果为 [{0.name}, {result}] </code>
     */
    public String[] getTemplateExpression() {
        if (_templateExpressions == null) {

            // get template from @Log
            List<String> result = new ArrayList<String>();

            int delim = template.indexOf(DELIM_START);

            if (delim > 0) {
                for (int i = delim + 1; i < template.length(); i++) {
                    char curChar = template.charAt(i);
                    if (curChar == DELIM_END) {
                        result.add(template.substring(delim, i + 1));
                    } else if (curChar == DELIM_START) {
                        delim = i;
                    }
                }
            }

            _templateExpressions = result;
        }

        return _templateExpressions.toArray(new String[_templateExpressions.size()]);
    }

    /**
     * 将原版的表达式中的表达式部分使用占位符号替换
     */
    public String getPlaceholdTemplate() {
        if (_placeholdTemplate == null) {

            String templateCopy = template;
            String[] templateExpression = getTemplateExpression();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < templateExpression.length; i++) {
                String curExp = templateExpression[i];
                int templateIndex = templateCopy.indexOf(curExp);
                sb.append(templateCopy.substring(0, templateIndex)).append(DELIM_START).append(i).append(DELIM_END);
                templateCopy = templateCopy.substring(templateIndex + curExp.length());
            }
            sb.append(templateCopy);
            _placeholdTemplate = sb.toString();
        }

        return _placeholdTemplate;
    }

    public boolean hasIdExpression() {
        return idExpression != null && !"".equals(idExpression);
    }

    protected static final String readMethodName(String name) {
        return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}