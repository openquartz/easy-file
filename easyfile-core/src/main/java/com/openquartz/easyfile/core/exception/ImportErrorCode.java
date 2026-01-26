package com.openquartz.easyfile.core.exception;

import lombok.Getter;
import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import com.openquartz.easyfile.core.executor.BaseAsyncImportExecutor;

/**
 * Import Error Code
 *
 * @author svnee
 */
@Getter
public enum ImportErrorCode implements EasyFileErrorCode {

    BASE_IMPORT_EXECUTOR_IMPL_ILL_ERROR("01", "class:{0} must implement " + BaseAsyncImportExecutor.class, true),
    IMPORT_CODE_NOT_UNIQ_ERROR("02", "importCode:({0}) not unique, please check it!", true),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_PREFIX = "Import-";

    ImportErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
