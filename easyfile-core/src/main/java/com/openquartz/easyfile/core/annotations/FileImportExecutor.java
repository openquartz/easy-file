package com.openquartz.easyfile.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.openquartz.easyfile.common.util.StringUtils;

/**
 * 文件导入执行器
 * 标记导入执行器
 *
 * @author svnee
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FileImportExecutor {

    /**
     * 执行器code 需要唯一
     */
    String value();

    /**
     * 执行器中文解释
     * 默认是{@link #value()}
     */
    String desc() default StringUtils.EMPTY;

    /**
     * 最大服务端重试次数
     * 小于等于0时不在执行重试。
     */
    int maxServerRetry() default 0;
}
