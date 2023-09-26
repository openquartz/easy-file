package com.openquartz.easyfile.core.annotations;

import com.openquartz.easyfile.common.util.StringUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件导入执行器
 * 标记导入执行器
 * <p>
 * 例如：
 * <pre> {@code
 * @Component
 * @FileImportExecutor("StudentImportDemoExecutor")
 * public class StudentImportDemoExecutor extends AbstractImportExcel07Executor {
 *
 *     @Resource
 *     private StudentMapper studentMapper;
 *
 *     @Override
 *     public boolean enableAsync(BaseImportRequestContext context) {
 *         return true;
 *     }
 *
 *     @Override
 *     public void importData(ImportRequestContext context) {
 *          // do import to i/o stream
 *     }
 * }}</pre>
 * </p>
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
     * 是否开启通知
     */
    boolean enableNotify() default false;
}

