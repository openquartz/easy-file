package org.svnee.easyfile.storage.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 上传异常码
 *
 * @author svnee
 */
public enum RemoteUploadExceptionCode implements EasyFileErrorCode {

    UPLOAD_FAIL("01", "上传失败"),
    UPLOAD_CALLBACK_ERROR("02", "上传结果回传失败"),
    REGISTER_DOWNLOAD_ERROR("03", "注册下载任务列表失败"),
    ENABLE_RUNNING_RESPONSE_ERROR("04", "开启运行任务异常"),
    LIST_DOWNLOAD_RECORD_ERROR("05", "下载记录查询异常"),
    UPLOAD_CANCEL_ERROR("06", "撤销下载异常"),
    DOWNLOAD_RESPONSE_ERROR("07", "下载调用远程服务响应异常"),
    ;

    private final String errorCode;

    private final String errorMsg;

    private static final String SIMPLE_BASE_CODE = "RemoteUploadError-";

    RemoteUploadExceptionCode(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }
}
