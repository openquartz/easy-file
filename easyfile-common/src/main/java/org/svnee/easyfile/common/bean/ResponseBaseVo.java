package org.svnee.easyfile.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import org.springframework.util.StringUtils;

/**
 * ResponseBaseVO
 *
 * @param <T> T
 * @author svnee
 */
public class ResponseBaseVo<T> implements Serializable {

    private String message;
    private String code;
    private String timestamp;
    private boolean success;
    @JsonInclude(Include.NON_NULL)
    private T datas;
    @JsonInclude(Include.NON_EMPTY)
    private Object requestValue;
    private String errorInfo;

    protected ResponseBaseVo(final boolean success, final String code, String message, T datas) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.datas = datas;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    protected ResponseBaseVo(final boolean success, final String code, String message, T datas, Object requestValue) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.datas = datas;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    protected ResponseBaseVo(final boolean success, final String code, String message, T datas, Object requestValue,
        String errorInfo) {
        this.message = message;
        this.code = code;
        this.success = success;
        this.datas = datas;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.errorInfo = errorInfo;
    }

    public ResponseBaseVo() {
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

    public T getDatas() {
        return this.datas;
    }

    public void setDatas(T datas) {
        this.datas = datas;
    }

    public Object getRequestValue() {
        return this.requestValue;
    }

    public void setRequestValue(Object requestValue) {
        this.requestValue = requestValue;
    }

    public String getMessage() {
        return StringUtils.isEmpty(this.message) ? (this.isSuccess() ? "请求成功" : this.message) : this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ResponseBaseVo ok() {
        return new ResponseBaseVo(true, "200", (String) null, (Object) null);
    }

    public static <T> ResponseBaseVo<T> ok(T datas) {
        return new ResponseBaseVo(true, "200", (String) null, datas);
    }

    public static ResponseBaseVo fail(String errorCode, String message) {
        return new ResponseBaseVo(false, errorCode, message, (Object) null);
    }

    public static <T> ResponseBaseVo<T> fail(String errorCode, String message, T datas) {
        return new ResponseBaseVo(false, errorCode, message, datas);
    }

    public static <T> ResponseBaseVo<T> fail(String errorCode, String message, T datas, String errorInfo) {
        return new ResponseBaseVo(false, errorCode, message, datas, (Object) null, errorInfo);
    }
}