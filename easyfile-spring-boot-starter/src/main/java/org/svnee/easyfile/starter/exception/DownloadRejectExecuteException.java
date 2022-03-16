package org.svnee.easyfile.starter.exception;

/**
 * @author svnee
 * @desc 下载拒绝执行异常
 **/
public class DownloadRejectExecuteException extends EasyFileException {

    public DownloadRejectExecuteException(EasyFileErrorCode errorCode) {
        super(errorCode);
    }
}
