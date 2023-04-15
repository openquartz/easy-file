package com.openquartz.easyfile.core.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;

/**
 * 下载拒绝执行异常
 * @author svnee
 **/
public class DownloadRejectExecuteException extends EasyFileException {

    public DownloadRejectExecuteException(EasyFileErrorCode errorCode) {
        super(errorCode);
    }
}
