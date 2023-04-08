package com.openquartz.easyfile.server.config;

import java.text.MessageFormat;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.server.exception.CommonExceptionCode;

/**
 * 全局异常捕获器.
 *
 * @author svnee
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {EasyFileException.class})
    public ResponseResult<?> serviceException(HttpServletRequest request, EasyFileException ex) {
        if (ex.getCause() != null) {
            log.error("[{}] {} [ex]", request.getMethod(), request.getRequestURL().toString(), ex);
            return ResponseResult.fail(ex.getErrorCode().getErrorCode(), ex.getMessage());
        }

        log.info("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString());
        return ResponseResult.fail(ex.getErrorCode().getErrorCode(), ex.getMessage());
    }

    @SneakyThrows
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseResult<?> validExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        FieldError firstFieldError = CollectionUtils.getFirst(bindingResult.getFieldErrors());
        String exceptionStr = Optional.ofNullable(firstFieldError)
            .map(FieldError::getDefaultMessage)
            .orElse(StringUtils.EMPTY);

        log.error("[{}] {} [ex] {}", request.getMethod(), getUrl(request), exceptionStr);
        return ResponseResult.fail(CommonExceptionCode.ILLEGAL_PARAM_ERROR.getErrorCode(),
            MessageFormat.format(CommonExceptionCode.ILLEGAL_PARAM_ERROR.getErrorMsg(), exceptionStr));
    }

    @ExceptionHandler(value = Throwable.class)
    public ResponseResult<?> defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("[{}] {} ", request.getMethod(), getUrl(request), throwable);
        return ResponseResult.fail(CommonExceptionCode.DEFAULT_ERROR.getErrorCode(), CommonExceptionCode.DEFAULT_ERROR
            .getErrorMsg());
    }

    private String getUrl(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getQueryString())) {
            return request.getRequestURL().toString();
        }

        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }

}
