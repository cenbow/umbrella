package com.harmony.umbrella.autoconfigure.core;

import com.harmony.umbrella.context.ApplicationConfiguration;
import com.harmony.umbrella.context.ApplicationConfigurationBuilder;
import com.harmony.umbrella.context.ApplicationContext;
import com.harmony.umbrella.context.WebXmlConstant;
import com.harmony.umbrella.web.context.WebApplicationSpringInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author wuxii@foxmail.com
 */
@Configuration
@ConditionalOnClass({ApplicationContext.class, ApplicationConfiguration.class})
@EnableConfigurationProperties(AppProperties.class)
public class AppAutoConfiguration {

    private static final String WEB_APPLICATION_SPRING_INITIALIZER = "com.harmony.umbrella.web.context.WebApplicationSpringInitializer";

    private final AppProperties appProperties;
    private final ServletContext servletContext;
    private final DataSource dataSource;

    public AppAutoConfiguration(AppProperties appProperties,
                                @Autowired(required = false) ServletContext servletContext,
                                @Autowired(required = false) DataSource dataSource) {
        this.servletContext = servletContext;
        this.appProperties = appProperties;
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnMissingBean(name = WEB_APPLICATION_SPRING_INITIALIZER)
    @ConditionalOnMissingClass(WEB_APPLICATION_SPRING_INITIALIZER)
    ApplicationRunner appInitializer() {
        return args -> {
            try {
                // shutdown first
                if (ApplicationContext.isStarted()) {
                    ApplicationContext.stop(true);
                }
                ApplicationContext.start(appConfig());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    @Bean(name = WEB_APPLICATION_SPRING_INITIALIZER)
    @ConditionalOnWebApplication
    @ConditionalOnClass(name = WEB_APPLICATION_SPRING_INITIALIZER)
    ServletContextInitializer webAppInitializer() throws NamingException, ServletException {
        WebApplicationSpringInitializer initializer = new WebApplicationSpringInitializer();
        initializer.setApplicationConfiguration(appConfig());
        return servletContext -> {
            // shutdown first
            if (ApplicationContext.isStarted()) {
                ApplicationContext.stop(true);
            }
            initializer.onStartup(servletContext);
        };
    }

    private ApplicationConfiguration appConfig() throws NamingException, ServletException {
        ApplicationConfigurationBuilder builder = ApplicationConfigurationBuilder.newBuilder();
        if (servletContext != null) {
            builder.apply(servletContext);
        }
        if (dataSource != null) {
            builder.addDataSource(dataSource);
        }
        if (appProperties.getDatasources() != null) {
            for (String jndi : appProperties.getDatasources()) {
                builder.addDataSource(jndi);
            }
        }
        if (appProperties.getInitializer() != null) {
            builder.setApplicationContextInitializer(appProperties.getInitializer());
        }
        if (appProperties.getPackages() != null) {
            for (String pkg : appProperties.getPackages()) {
                builder.addScanPackage(pkg);
            }
        }
        if (appProperties.getShutdownHooks() != null) {
            for (Class<? extends Runnable> cls : appProperties.getShutdownHooks()) {
                builder.addShutdownHook(cls);
            }
        }

        Properties properties = appProperties.getProperties();
        if (properties != null) {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                builder.addProperty(WebXmlConstant.APPLICATION_CFG_PROPERTIES + "." + entry.getKey().toString(), entry.getValue());
            }
        }

        return builder.build();
    }


}
