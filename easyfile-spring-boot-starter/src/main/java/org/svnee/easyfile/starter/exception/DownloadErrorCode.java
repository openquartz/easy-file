package org.svnee.easyfile.starter.exception;

import lombok.Getter;

/**
 * @author svnee
 * @desc 下载异常码
 **/
@Getter
public enum DownloadErrorCode implements EasyFileErrorCode {

    /*超过队列前缀*/
    DOWNLOAD_OVER_WAIT_NUM_REJECT("01", "超过队列等待数拒绝下载"),

    ;
    /** 异常码 */
    private final String errorCode;
    /** 异常信息 */
    private final String errorMsg;

    /** 基础码前缀 */
    private static final String SIMPLE_BASE_PREFIX = "Download-";

    DownloadErrorCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
    }
}
