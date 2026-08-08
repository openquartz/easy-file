package com.openquartz.easyfile.core.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import lombok.Getter;

/**
 * 导入异常码
 *
 * @author svnee
 */
@Getter
public enum ImportErrorCode implements EasyFileErrorCode {

    FILE_IMPORT_EXECUTOR_MUST_SUPPORT_ANNOTATION("01", "文件导入执行器必须使用@FileImportExecutor注解"),
    SYNC_IMPORT_EXECUTE_ERROR("02", "同步导入执行异常"),
    BASE_IMPORT_EXECUTOR_IMPL_ILL_ERROR("03",
        "class:{0} must implement " + BaseImportExecutor.class, true),

    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_PREFIX = "Import-";

    ImportErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    ImportErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
