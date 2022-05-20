package org.svnee.easyfile.common.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * SpringContextUtil
 *
 * @author svnee
 */
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    private SpringContextUtil() {
    }

    public static Class<?> getRealClass(Object springBean) {
        return AopUtils.isAopProxy(springBean) ? AopUtils.getTargetClass(springBean) : springBean.getClass();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.context = applicationContext;
    }

    /**
     * Get ioc container bean by type.
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * Get ioc container bean by name and type.
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    /**
     * Get a set of ioc container beans by type.
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }

    /**
     * Find whether the bean has annotations.
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return context.findAnnotationOnBean(beanName, annotationType);
    }

    /**
     * Get ApplicationContext.
     */
    public static ApplicationContext getInstance() {
        return context;
    }

}
