package org.svnee.easyfile.admin.exception.handler;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;

/**
 * common exception handler
 *
 * @author svnee
 */
@ControllerAdvice(basePackages = "org.svnee.easyfile")
public class CommonExceptionHandler extends AbstractExceptionHandler {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseResult<?>> handleMessageNotReadableException(Exception e) {

        return handler(e, CommonErrorCode.REQUEST_PARAM_ERROR, LogLevel.WARN);
    }

    @ExceptionHandler(EasyFileException.class)
    public ResponseEntity<ResponseResult<?>> handlerBusiness(EasyFileException e) {
        // 根据code　判断是否需要使用异常信息覆盖
        if (e.getErrorCode().isReplacePlaceHold()) {
            return handler(e, e.getErrorCode(), e.getMessage(), LogLevel.WARN);
        } else {
            return handler(e, e.getErrorCode(), LogLevel.WARN);
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseResult<?>> handlerBusiness(MethodArgumentTypeMismatchException e) {
        return handler(e, CommonErrorCode.REQUEST_PARAM_ERROR, LogLevel.WARN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseResult<?>> handlerBusiness(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        String message = constraintViolations.stream().map(ConstraintViolation::getMessageTemplate)
            .collect(Collectors.joining(","));

        return handler(e, CommonErrorCode.REQUEST_PARAM_ERROR, message, LogLevel.WARN);
    }
}