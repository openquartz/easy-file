package com.openquartz.easyfile.core.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 错误记录
 *
 * @author svnee
 */
@Data
@Builder
public class ErrorRecord {

    /**
     * 行号
     */
    private Integer rowNumber;

    /**
     * 行键值
     */
    private String rowKey;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 建议操作
     */
    private String suggestedAction;

    public static ErrorRecord of(Integer rowNumber, String errorMessage) {
        return ErrorRecord.builder()
                .rowNumber(rowNumber)
                .errorMessage(errorMessage)
                .suggestedAction("请检查数据格式并重试")
                .build();
    }

    public static ErrorRecord of(String rowKey, String errorMessage) {
        return ErrorRecord.builder()
                .rowKey(rowKey)
                .errorMessage(errorMessage)
                .suggestedAction("请检查数据格式并重试")
                .build();
    }
}