package com.openquartz.easyfile.admin.exception.handler;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * Exception-Handler
 *
 * @author svnee
 * @since 1.2.0
 */
@Slf4j
public class AbstractExceptionHandler {

    protected ResponseEntity<ResponseResult<?>> handler(EasyFileErrorCode e) {
        ResponseResult<?> responseBean = new ResponseResult<>();
        responseBean.setCode(e.getErrorCode());
        responseBean.setMessage(e.getErrorMsg());
        responseBean.setSuccess(false);
        responseBean.setTimestamp((new Date()).toString());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseBean);

    }

    protected ResponseEntity<ResponseResult<?>> handler(Throwable e, EasyFileErrorCode code,
        LogLevel logLevel) {
        return handler(e, code, code.getErrorMsg(), logLevel);
    }

    protected ResponseEntity<ResponseResult<?>> handler(Throwable e, EasyFileErrorCode code,
        String errorMessage,
        LogLevel logLevel) {
        Throwable rootCause = getRootCause(e);

        String message = errorMessage != null ? errorMessage
            : code.getErrorMsg() != null ? code.getErrorMsg() : rootCause == null ? null : rootCause.getMessage();
        ResponseResult<?> responseBean = new ResponseResult<>();
        responseBean.setCode(code.getErrorCode());
        responseBean.setMessage(message);
        responseBean.setSuccess(false);
        responseBean.setTimestamp((new Date()).toString());

        // 默认设置http status 为 ok
        return handler(e, HttpStatus.OK, responseBean, logLevel);
    }

    protected ResponseEntity<ResponseResult<?>> handler(
        Throwable e, HttpStatus httpStatus, ResponseResult<?> responseBean, LogLevel logLevel) {
        logException(e, responseBean, logLevel);

        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(responseBean);
    }

    private void logException(Throwable e, ResponseResult<?> responseBean, LogLevel logLevel) {
        if (logLevel == LogLevel.ERROR) {
            log.error("[{}] {}", responseBean.getCode(), responseBean.getMessage(), e);
        } else if (logLevel == LogLevel.WARN) {
            log.warn("[{}] {}", responseBean.getCode(), responseBean.getMessage(), e);
        } else if (logLevel == LogLevel.INFO) {
            log.info("[{}] {}", responseBean.getCode(), responseBean.getMessage());
        }
    }

    private Throwable getRootCause(Throwable e) {
        if (e == null || e.getCause() == null || e.getCause().getMessage() == null) {
            return e;
        }
        if (isWrapperException(e)) {
            return getRootCause(e.getCause());
        }
        return e;
    }

    private boolean isWrapperException(Throwable e) {
        return e instanceof UndeclaredThrowableException;
    }


}
