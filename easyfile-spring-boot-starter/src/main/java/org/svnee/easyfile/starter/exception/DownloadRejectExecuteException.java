package org.svnee.easyfile.starter.exception;

/**
 * @author svnee
 * @desc 下载拒绝执行异常
 **/
public class DownloadRejectExecuteException extends EasyFileException {

    private final String errorMsg;

    public DownloadRejectExecuteException(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
