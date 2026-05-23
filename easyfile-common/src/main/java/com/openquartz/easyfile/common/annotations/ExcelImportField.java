package com.openquartz.easyfile.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel导入字段注解
 * 用于标记Excel导入时的字段映射关系
 *
 * @author svnee
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelImportField {

    /**
     * 列索引 (从0开始)
     */
    int columnIndex() default -1;

    /**
     * 列标题名称 (用于验证)
     */
    String columnHeader() default "";

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 日期格式
     */
    String dateFormat() default "yyyy-MM-dd";

    /**
     * 自定义验证器
     */
    Class<?> validator() default void.class;

    /**
     * 错误提示信息
     */
    String errorMessage() default "字段格式不正确";
}
