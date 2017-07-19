package com.harmony.umbrella.web;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.harmony.umbrella.data.query.JpaQueryBuilder;
import com.harmony.umbrella.json.Json;
import com.harmony.umbrella.util.IOUtils;
import com.harmony.umbrella.web.method.support.QueryParameter;

/**
 * @author wuxii@foxmail.com
 */
public class QueryParameterTest {

    private String parameters;

    @Before
    public void before() throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource("query/params.json");
        InputStream is = resource.getInputStream();
        parameters = IOUtils.toString(is);
        is.close();
    }

    @Test
    public void test() {
        QueryParameter parameter = Json.parse(parameters, QueryParameter.class);
        System.out.println(parameter);

        JpaQueryBuilder<Object> builder = new JpaQueryBuilder<>();
        parameter.apply(builder);

        System.out.println(builder.bundle());
    }

}
