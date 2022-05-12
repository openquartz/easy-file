package org.svnee.easyfile.starter.exception;

import lombok.Getter;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;

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
    DOWNLOAD_FILE_NAME_ENCODING_ERROR("05", "下载文件名Encoding异常"),
    BASE_DOWNLOAD_EXECUTOR_IMPL_ILL_ERROR("06",
        "class:{0} must implement " + BaseDownloadExecutor.class, true),
    DOWNLOAD_CODE_NOT_UNIQ_ERROR("07", "downloadCode:({0}) not unique,please check it!", true),

    ;
    /** 异常码 */
    private final String errorCode;
    /** 异常信息 */
    private final String errorMsg;
    private final boolean replacePlaceHold;

    /** 基础码前缀 */
    private static final String SIMPLE_BASE_PREFIX = "Download-";

    DownloadErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    DownloadErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
