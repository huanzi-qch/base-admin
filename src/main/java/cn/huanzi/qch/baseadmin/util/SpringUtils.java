package cn.huanzi.qch.baseadmin.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring bean工具类
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    //应用上下文
    private static ApplicationContext APPLICATION_CONTEXT = null;

    private SpringUtils() {}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.APPLICATION_CONTEXT == null) {
            SpringUtils.APPLICATION_CONTEXT = applicationContext;
        }
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
}
