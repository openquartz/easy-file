package org.svnee.easyfile.core.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;

/**
 * 下载拒绝执行异常
 * @author svnee
 **/
public class DownloadRejectExecuteException extends EasyFileException {

    public DownloadRejectExecuteException(EasyFileErrorCode errorCode) {
        super(errorCode);
    }
}
