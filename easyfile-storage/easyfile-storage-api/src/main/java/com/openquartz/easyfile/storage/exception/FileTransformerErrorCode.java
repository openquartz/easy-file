package com.openquartz.easyfile.storage.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 文件url 转换异常码
 * @author svnee
 */
public enum FileTransformerErrorCode implements EasyFileErrorCode {
    FILE_TRANSFORM_REPEAT_ERROR("01","系统标识:{0}的FileUrlTransformer重复!",true),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    FileTransformerErrorCode(String errorCode, String errorMsg) {
        this(errorCode,errorMsg,false);
    }

    FileTransformerErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public boolean isReplacePlaceHold() {
        return replacePlaceHold;
    }


}
