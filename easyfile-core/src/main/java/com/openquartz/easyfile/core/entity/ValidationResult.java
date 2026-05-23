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

    public static ValidationResult of(int totalCount, int errorCount) {
        return ValidationResult.builder()
                .success(errorCount == 0)
                .errorCount(errorCount)
                .successCount(totalCount - errorCount)
                .build();
    }
}