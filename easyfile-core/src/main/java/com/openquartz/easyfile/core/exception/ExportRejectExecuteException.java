package com.openquartz.easyfile.core.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;

/**
 * 下载拒绝执行异常
 * @author svnee
 **/
public class ExportRejectExecuteException extends EasyFileException {

    public ExportRejectExecuteException(EasyFileErrorCode errorCode) {
        super(errorCode);
    }
}
