package com.openquartz.easyfile.core.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 验证结果
 *
 * @author svnee
 */
@Data
@Builder
public class ValidationResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误列表
     */
    private List<ErrorRecord> errors;

    /**
     * 错误数量
     */
    private int errorCount;

    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 验证通过、可继续业务处理的数据（可选；未设置时由引擎按错误行号过滤）
     */
    private List<?> validData;

    public static ValidationResult success() {
        return ValidationResult.builder()
                .success(true)
                .errorCount(0)
                .successCount(0)
                .build();
    }

    public static ValidationResult failure(List<ErrorRecord> errors) {
        return ValidationResult.builder()
                .success(false)
                .errors(errors)
                .errorCount(errors.size())
                .successCount(0)
                .build();
    }

    public static <T> ValidationResult partial(List<ErrorRecord> errors, List<T> validData) {
        List<ErrorRecord> safeErrors = errors == null ? java.util.Collections.emptyList() : errors;
        List<T> safeValid = validData == null ? java.util.Collections.emptyList() : validData;
        return ValidationResult.builder()
                .success(safeErrors.isEmpty())
                .errors(safeErrors)
                .errorCount(safeErrors.size())
                .successCount(safeValid.size())
                .validData(safeValid)
                .build();
    }

    public static ValidationResult of(int totalCount, int errorCount) {
        return ValidationResult.builder()
                .success(errorCount == 0)
                .errorCount(errorCount)
                .successCount(totalCount - errorCount)
                .build();
    }
}