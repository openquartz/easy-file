package org.svnee.easyfile.starter.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;

/**
 * @author svnee
 * @desc 下载拒绝执行异常
 **/
public class DownloadRejectExecuteException extends EasyFileException {

    public DownloadRejectExecuteException(EasyFileErrorCode errorCode) {
        super(errorCode);
    }
}
