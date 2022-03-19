package org.svnee.easyfile.common.exception;

/**
 * 数据执行-异常码
 *
 * @author svnee
 */
public enum DataExecuteErrorCode implements EasyFileErrorCode {

    INSERT_ERROR("01", "数据插入失败"),
    UPDATE_ERROR("02", "数据更新失败"),
    DELETE_ERROR("03", "数据删除失败"),
    NOT_EXIST_ERROR("04", "数据不存在异常"),
    ;
    private final String errorCode;
    private final String errorMsg;

    private static final String SIMPLE_BASE_CODE = "DataExecute-";

    DataExecuteErrorCode(String errorCode, String errorMsg) {
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
