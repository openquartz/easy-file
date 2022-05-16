package org.svnee.easyfile.common.bean.excel;

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

}
