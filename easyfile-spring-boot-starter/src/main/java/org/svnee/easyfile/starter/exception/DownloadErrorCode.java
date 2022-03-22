package org.svnee.easyfile.starter.exception;

import lombok.Getter;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 下载异常码
 *
 * @author svnee
 **/
@Getter
public enum DownloadErrorCode implements EasyFileErrorCode {

    /*超过队列前缀*/
    DOWNLOAD_OVER_WAIT_NUM_REJECT("01", "超过队列等待数拒绝下载"),
    /*文件下载执行器必须使用注解*/
    FILE_GENERATOR_MUST_SUPPORT_ANNOTATION("02", "文件下载执行器必须使用@FileExportExecutor注解"),
    SYNC_DOWNLOAD_EXECUTE_ERROR("03", "同步下载执行异常"),
    /*处理文件下载异常*/
    HANDLE_DOWNLOAD_FILE_ERROR("04", "处理下载文件异常"),

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
