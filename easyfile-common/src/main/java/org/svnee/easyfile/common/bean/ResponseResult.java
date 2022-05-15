package org.svnee.easyfile.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * Result
 *
 * @param <T> T
 * @author svnee
 */
public class ResponseResult<T> implements Serializable {

    private String message;
    private String code;
    private String timestamp;
    private boolean success;
    @JsonInclude(Include.NON_NULL)
    private T data;
    @JsonInclude(Include.NON_EMPTY)
    private Object requestValue;
    private String errorInfo;

    protected ResponseResult(final boolean success, final String code, String message, T data) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.data = data;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    protected ResponseResult(final boolean success, final String code, String message, T data, Object requestValue) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.data = data;
        this.requestValue = requestValue;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    protected ResponseResult(final boolean success, final String code, String message, T data, Object requestValue,
        String errorInfo) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.data = data;
        this.requestValue = requestValue;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.errorInfo = errorInfo;
    }

    public ResponseResult() {
    }

    public String getCode() {
        return this.isSuccess() ? "0" : this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public T getData() {
        return data;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public Object getRequestValue() {
        return this.requestValue;
    }

    public void setRequestValue(Object requestValue) {
        this.requestValue = requestValue;
    }

    public String getMessage() {
        if (StringUtils.isBlank(this.message)) {
            return this.isSuccess() ? "请求成功" : this.message;
        }
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static <T> ResponseResult<T> ok() {
        return new ResponseResult<>(true, "200", null, null);
    }

    public static <T> ResponseResult<T> ok(T data) {
        return new ResponseResult<>(true, "200", null, data);
    }

    public static <T> ResponseResult<T> fail(String errorCode, String message) {
        return new ResponseResult<>(false, errorCode, message, null);
    }

    public static <T> ResponseResult<T> fail(String errorCode, String message, T data) {
        return new ResponseResult<>(false, errorCode, message, data);
    }

    public static <T> ResponseResult<T> fail(String errorCode, String message, T data, String errorInfo) {
        return new ResponseResult<>(false, errorCode, message, data, null, errorInfo);
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
            "message='" + message + '\'' +
            ", code='" + code + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", success=" + success +
            ", data=" + data +
            ", requestValue=" + requestValue +
            ", errorInfo='" + errorInfo + '\'' +
            '}';
    }
}