package com.openquartz.easyfile.core.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 分页导入结果
 *
 * @author svnee
 */
@Data
@Builder
public class ImportPagedResult {

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 成功记录数
     */
    private int successCount;

    /**
     * 失败记录数
     */
    private int failCount;

    /**
     * 错误记录列表
     */
    private List<ErrorRecord> errorRecords;

    public static ImportPagedResult empty() {
        return ImportPagedResult.builder()
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .build();
    }

    public static ImportPagedResult of(long totalCount, int successCount, int failCount, List<ErrorRecord> errorRecords) {
        return ImportPagedResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .errorRecords(errorRecords)
                .build();
    }

    public boolean isComplete() {
        return successCount + failCount >= totalCount;
    }

    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount;
    }
}