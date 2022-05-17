package org.svnee.easyfile.common.bean.excel;

import java.util.Collection;

/**
 * 反射工具类
 *
 * @author svnee
 **/
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * 是否是JAVA class
     *
     * @param clazz clazz
     * @return 是否是java class
     */
    public static <T> boolean isJavaClass(Class<T> clazz) {
        String name = clazz.getName();
        return name.startsWith("java");
    }

    /**
     * 是否是java 基础类型（java开头的非基础类型）
     *
     * @param clazz clazz
     * @param <T> T
     * @return 是否符合
     */
    public static <T> boolean isBaseJavaClass(Class<T> clazz) {
        return isJavaClass(clazz) && !isCollection(clazz);
    }

    /**
     * 是否是集合
     *
     * @param clazz clazz
     * @param <T> T
     * @return 是否是集合
     */
    public static <T> boolean isCollection(Class<T> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

}
