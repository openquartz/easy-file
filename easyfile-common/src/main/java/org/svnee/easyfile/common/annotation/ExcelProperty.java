package org.svnee.easyfile.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.groups.Default;

/**
 * @author svnee
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelProperty {

    /**
     * 对应的字段名(表头名)
     *
     * @return {@link String}
     */
    String value() default "";

    /**
     * 顺序值
     * excel上的列的顺序（默认为类中属性的顺序）
     */
    int order() default Integer.MAX_VALUE;

    /**
     * 列宽
     */
    int width() default 2048;

    /**
     * 日期格式
     * 针对日期属性序列化的格式 例如：yyyy-MM-dd
     * 默认是yyyy-MM-dd HH:mm:ss
     */
    String dateFormatter() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认分组
     */
    Class<?>[] group() default Default.class;

}
