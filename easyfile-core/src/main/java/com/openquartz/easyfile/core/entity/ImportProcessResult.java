package com.openquartz.easyfile.core.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 导入处理结果
 *
 * @author svnee
 */
@Data
@Builder
public class ImportProcessResult {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 是否成功
     */
    private boolean success;

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

    /**
     * 临时文件路径
     */
    private String tempFilePath;

    /**
     * 是否为幂等性返回
     */
    private boolean idempotent = false;

    /**
     * 是否完成
     */
    private boolean complete;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 执行耗时 (毫秒)
     */
    private Long duration;
}