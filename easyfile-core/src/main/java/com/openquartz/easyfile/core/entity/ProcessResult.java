package com.openquartz.easyfile.core.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 处理结果
 *
 * @author svnee
 */
@Data
@Builder
public class ProcessResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 处理数量
     */
    private int processCount;

    /**
     * 错误列表
     */
    private List<ErrorRecord> errors;

    public static ProcessResult success(int count) {
        return ProcessResult.builder()
                .success(true)
                .processCount(count)
                .build();
    }

    public static ProcessResult failure(int count, List<ErrorRecord> errors) {
        return ProcessResult.builder()
                .success(false)
                .processCount(count)
                .errors(errors)
                .build();
    }

    public static ProcessResult of(boolean success, int count) {
        return ProcessResult.builder()
                .success(success)
                .processCount(count)
                .build();
    }
}