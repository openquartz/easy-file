package com.openquartz.easyfile.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.exception.CommonErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;

/**
 * 反射工具类
 *
 * @author svnee
 **/
@Slf4j
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

    /**
     * 获取Obj的属性的Get-Value
     *
     * @param obj obj
     * @param filed field
     * @return value
     */
    public static Object reflectiveGetFieldValue(Object obj, Field filed) {
        Class<?> clazz = obj.getClass();
        try {
            Method getterMethod;
            if (boolean.class.isAssignableFrom((filed.getType()))) {
                getterMethod = org.springframework.util.ReflectionUtils
                    .findMethod(clazz, booleanGetterMethodName(filed.getName()));
            } else {
                getterMethod = org.springframework.util.ReflectionUtils
                    .findMethod(clazz, commonGetterMethodName(filed.getName()));
            }
            assert getterMethod != null;
            return getterMethod.invoke(obj);
        } catch (Exception e) {
            log.error("ReflectionUtils#reflectiveGetFieldValue,obj:{},fieldName:{}", obj, filed.getName(), e);
            throw new EasyFileException(CommonErrorCode.DOWNLOAD_EXECUTE_REFLECT_ERROR);
        }
    }

    private static String booleanGetterMethodName(String fieldName) {
        return "is" + firstIndexToUpper(fieldName);
    }

    private static String commonGetterMethodName(String fieldName) {
        return "get" + firstIndexToUpper(fieldName);
    }

    private static String firstIndexToUpper(String str) {
        if (str == null || str.length() <= 0) {
            return StringUtils.EMPTY;
        }
        String first = str.charAt(0) + StringUtils.EMPTY;
        return str.replaceFirst(first, first.toUpperCase());
    }

}
