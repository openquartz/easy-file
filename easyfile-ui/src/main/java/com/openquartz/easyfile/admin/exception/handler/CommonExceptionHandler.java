package com.openquartz.easyfile.admin.exception.handler;

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
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.exception.CommonErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;

/**
 * common exception handler
 *
 * @author svnee
 * @since 1.2.0
 */
@ControllerAdvice(basePackages = Constants.BASE_PACKAGE_PATH)
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