package cn.huanzi.qch.baseadmin.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring bean工具类
 */
@Component
public class SpringUtils implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    //应用上下文
    private static ApplicationContext APPLICATION_CONTEXT = null;

    private static BeanDefinitionRegistry beanDefinitionRegistry;

    private SpringUtils() {}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.APPLICATION_CONTEXT == null) {
            SpringUtils.APPLICATION_CONTEXT = applicationContext;
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        SpringUtils.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    /**
     * 通过name获取 Bean.
     */
    public static Object getBean(String name) {
        return SpringUtils.APPLICATION_CONTEXT.getBean(name);
    }

    /**
     * 通过class获取Bean.
     */
    public static <T> T getBean(Class<T> clazz) {
        return SpringUtils.APPLICATION_CONTEXT.getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return SpringUtils.APPLICATION_CONTEXT.getBean(name, clazz);
    }

    /**
     * 注册bean
     */
    public static void registerBean(String name, Class<?> beanClass) {
        beanDefinitionRegistry.registerBeanDefinition(name, new RootBeanDefinition(beanClass));
    }
}
