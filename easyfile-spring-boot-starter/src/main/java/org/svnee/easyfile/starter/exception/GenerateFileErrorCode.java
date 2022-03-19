package org.svnee.easyfile.starter.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 生成文件异常码
 *
 * @author svnee
 */
public enum GenerateFileErrorCode implements EasyFileErrorCode {

    /*生成文件名重复异常*/
    FILE_NAME_DUPLICATE_ERROR("01", "生成文件名重复"),
    /*本地临时文件目录创建失败*/
    CREATE_LOCAL_TEMP_FILE_ERROR("02", "创建本地临时目录失败"),
    ;

    /**
     * 异常码前缀
     */
    private static final String SIMPLE_BASE_CODE_PREFIX = "GenFileError-";

    /**
     * 异常码
     */
    private final String errorCode;

    /**
     * 异常码
     */
    private final String errorMsg;

    GenerateFileErrorCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_CODE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
