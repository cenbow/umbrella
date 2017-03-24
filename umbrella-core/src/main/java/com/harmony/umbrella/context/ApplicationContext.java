package com.harmony.umbrella.context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import com.harmony.umbrella.asm.ClassReader;
import com.harmony.umbrella.context.metadata.ApplicationMetadata;
import com.harmony.umbrella.context.metadata.DatabaseMetadata;
import com.harmony.umbrella.context.metadata.ServerMetadata;
import com.harmony.umbrella.core.BeanFactory;
import com.harmony.umbrella.core.BeansException;
import com.harmony.umbrella.core.ConnectionSource;
import com.harmony.umbrella.core.SimpleBeanFactory;
import com.harmony.umbrella.log.Log;
import com.harmony.umbrella.log.Logs;
import com.harmony.umbrella.util.ClassFilter;
import com.harmony.umbrella.util.ClassFilterFeature;
import com.harmony.umbrella.util.IOUtils;

/**
 * 运行的应用的上下文
 * 
 * @author wuxii@foxmail.com
 */
public abstract class ApplicationContext implements BeanFactory {

    protected static final Log LOG = Logs.getLog(ApplicationContext.class);

    protected static final ThreadLocal<CurrentContext> current = new InheritableThreadLocal<CurrentContext>();

    private static final List<Class> classes = new Vector<Class>();

    private static final List<Runnable> shutdownHooks = new ArrayList<>();

    private static ServerMetadata serverMetadata = ApplicationMetadata.EMPTY_SERVER_METADATA;

    private static List<DatabaseMetadata> databaseMetadatas = new ArrayList<DatabaseMetadata>();

    private static ApplicationConfiguration applicationConfiguration;

    private static final String STANDBY = "standby";
    private static final String STARTED = "started";
    private static final String SHUTDOWN = "shutdown";

    private static final Object applicationStatusLock = new Object();

    private static String applicationStatus;

    public static void start(ApplicationConfiguration appConfig) {
        synchronized (applicationStatusLock) {
            if (!isStandBy()) {
                LOG.warn("application already started");
                return;
            }
            ApplicationContextInitializer applicationInitializer = null;
            Class<? extends ApplicationContextInitializer> applicationInitializerClass = appConfig.getApplicationContextInitializerClass();
            if (applicationInitializerClass == null) {
                applicationInitializerClass = ApplicationContextInitializer.class;
            }
            try {
                applicationInitializer = applicationInitializerClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("illegal application initializer class " + applicationInitializerClass);
            }
            // 初始化应用程序
            applicationInitializer.init(appConfig);
            applicationConfiguration = appConfig;
            applicationStatus = STARTED;
        }
    }

    public static void shutdown() {
        synchronized (applicationStatusLock) {
            try {
                for (Runnable runnable : shutdownHooks) {
                    runnable.run();
                }
            } catch (Throwable e) {
                if (!Boolean.valueOf(applicationConfiguration.getStringProperty("focus-showdown", "true"))) {
                    throw e;
                }
                LOG.error("shutdown hooks mount fail", e);
            }
            classes.clear();
            shutdownHooks.clear();
            databaseMetadatas.clear();
            applicationConfiguration = null;
        }
    }

    /**
     * 获取应用的初始化配置, 必须在启动后才能获取
     * 
     * @return 初始化配置
     */
    public static ApplicationConfiguration getApplicationConfiguration() {
        checkApplicationState();
        return applicationConfiguration;
    }

    /**
     * 判断应用是否已经启动
     * 
     * @return true is started
     */
    public static boolean isStarted() {
        synchronized (applicationStatusLock) {
            return applicationStatus == STARTED;
        }
    }

    public static boolean isStandBy() {
        synchronized (applicationStatusLock) {
            return applicationStatus == STANDBY;
        }
    }

    public static boolean isShutdown() {
        synchronized (applicationStatusLock) {
            return applicationStatus == SHUTDOWN;
        }
    }

    /**
     * 获取当前应用的应用上下文
     * <p>
     * 加载 {@code META-INF/services/com.huiju.module.context.ContextProvider}
     * 文件中的实际类型来创建
     *
     * @return 应用上下文
     */
    public static final ApplicationContext getApplicationContext() {
        checkApplicationState();
        ApplicationContext context = null;
        ServiceLoader<ApplicationContextProvider> providers = ServiceLoader.load(ApplicationContextProvider.class);
        for (ApplicationContextProvider provider : providers) {
            Map applicationProperties = new HashMap<>(applicationConfiguration.getApplicationProperties());
            context = provider.createApplicationContext(applicationProperties);
            if (context != null) {
                LOG.debug("create context [{}] by [{}]", context, provider);
                break;
            }
        }
        if (context == null) {
            context = SimpleApplicationContext.INSTANCE;
            LOG.debug("no context provider find, use default {}", SimpleApplicationContext.class.getName());
        }
        // 初始化
        context.init();
        return context;
    }

    /**
     * 获取当前线程的用户环境
     *
     * @return 用户环境
     */
    public static CurrentContext getCurrentContext() {
        checkApplicationState();
        return current.get();
    }

    public static ServerMetadata getServerMetadata() {
        checkApplicationState();
        return serverMetadata;
    }

    public static DatabaseMetadata[] getDatabaseMetadatas() {
        checkApplicationState();
        return databaseMetadatas.toArray(new DatabaseMetadata[0]);
    }

    /**
     * 设置当前线程的用户环境
     *
     * @param cc
     *            用户环境
     */
    static void setCurrentContext(CurrentContext cc) {
        checkApplicationState();
        current.set(cc);
    }

    static int getApplicationClassSize() {
        checkApplicationState();
        return classes.size();
    }

    public static Class[] getApplicationClasses(ClassFilter filter) {
        checkApplicationState();
        List<Class> result = new ArrayList<Class>();
        for (Class c : classes) {
            if (ClassFilterFeature.safetyAccess(filter, c)) {
                result.add(c);
            }
        }
        return result.toArray(new Class[result.size()]);
    }

    private static void checkApplicationState() {
        if (!isStarted()) {
            throw new IllegalStateException("application not started!");
        }
        if (isShutdown()) {
            throw new IllegalStateException("application already shutdown");
        }
    }
    
    // application bean scope

    protected ApplicationContext() {
    }

    public abstract BeanFactory getBeanFactory();

    /**
     * 初始化bean工厂
     */
    public abstract void init();

    /**
     * 销毁bean工厂
     */
    public abstract void destroy();

    @Override
    public void autowrie(Object bean) throws BeansException {
        getBeanFactory().autowrie(bean);
    }

    @Override
    public <T> T getBean(Class<T> beanClass) throws BeansException {
        return getBeanFactory().getBean(beanClass);
    }

    @Override
    public <T> T getBean(Class<T> beanClass, String scope) throws BeansException {
        return getBeanFactory().getBean(beanClass, scope);
    }

    @Override
    public <T> T getBean(String beanName) throws BeansException {
        return getBeanFactory().getBean(beanName);
    }

    @Override
    public <T> T getBean(String beanName, String scope) throws BeansException {
        return getBeanFactory().getBean(beanName, scope);
    }

    private static String readClassName(Resource resource) throws IOException {
        InputStream is = resource.getInputStream();
        byte[] b = IOUtils.toByteArray(is);
        try {
            is.close();
        } catch (IOException e) {
        }
        return new ClassReader(b).getClassName().replaceAll("/", ".");
    }

    public static class ApplicationContextInitializer {

        protected static final Log log = Logs.getLog(ApplicationContextInitializer.class);

        protected ApplicationContextInitializer() {
        }

        final void init(ApplicationConfiguration applicationConfiguration) {

            new InternalApplicationInitializer(applicationConfiguration).init();

            initCustomer(applicationConfiguration);
        }

        protected void initCustomer(ApplicationConfiguration applicationConfiguration) {

        }

    }

    private static final class InternalApplicationInitializer {

        private ApplicationConfiguration cfg;

        public InternalApplicationInitializer(ApplicationConfiguration applicationConfiguration) {
            this.cfg = applicationConfiguration;
        }

        public void init() {

            init_server();

            init_database();

            init_application_classes();

        }

        private void init_server() {
            ServletContext servletContext = cfg.getServletContext();
            if (servletContext == null) {
                LOG.warn("servlet context not set, server metadata could not be initialized");
                return;
            }
            serverMetadata = ApplicationMetadata.getServerMetadata(servletContext);
        }

        private void init_database() {
            List<ConnectionSource> css = cfg.getConnectionSources();
            if (css == null || css.isEmpty()) {
                LOG.warn("connection source not set, database metadata could not be initialized");
                return;
            }
            for (ConnectionSource cs : css) {
                try {
                    databaseMetadatas.add(ApplicationMetadata.getDatabaseMetadata(cs));
                } catch (SQLException e) {
                    LOG.error("initial database metadata failed", e);
                }
            }
        }

        private void init_application_classes() {
            Collection<String> packages = cfg.getScanPackages();
            if (packages != null && !packages.isEmpty()) {
                classes.clear();
                ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
                ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
                boolean initialize = Boolean.valueOf(cfg.getStringProperty("init-class-when-scan"));
                for (String pkg : packages) {
                    String path = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + pkg.replace(".", "/") + "/**/*.class";
                    try {
                        Resource[] resources = resourceLoader.getResources(path);
                        for (Resource resource : resources) {
                            String className = null;
                            try {
                                className = readClassName(resource);
                                Class<?> clazz = Class.forName(className, initialize, classLoader);
                                if (!classes.contains(clazz)) {
                                    classes.add(clazz);
                                }
                            } catch (IOException e) {
                                LOG.error("can't read resource {}", resource);
                            } catch (Error e) {
                                LOG.warn("{} in classpath jar no fully configured, {}", className, e.toString());
                            } catch (Throwable e) {
                                LOG.error("{}", className, e);
                            }
                        }
                    } catch (IOException e) {
                        LOG.error("{} package not found", pkg, e);
                    }
                }

                Collections.sort(classes, new Comparator<Class>() {

                    @Override
                    public int compare(Class o1, Class o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        }

    }

    private static final class SimpleApplicationContext extends ApplicationContext {

        private static final SimpleApplicationContext INSTANCE = new SimpleApplicationContext();
        private static BeanFactory beanFactory = SimpleBeanFactory.INSTANCE;

        public SimpleApplicationContext() {
        }

        @Override
        public void init() {

        }

        @Override
        public void destroy() {

        }

        @Override
        public BeanFactory getBeanFactory() {
            return beanFactory;
        }

    }
}
