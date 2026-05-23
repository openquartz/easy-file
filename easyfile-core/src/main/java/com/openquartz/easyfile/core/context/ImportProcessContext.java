package com.openquartz.easyfile.core.context;

import lombok.Builder;
import lombok.Data;

/**
 * 导入处理上下文
 * 包含业务处理的上下文信息
 *
 * @author svnee
 */
@Data
@Builder
public class ImportProcessContext {

    /**
     * 导入上下文
     */
    private ImportContext requestContext;

    /**
     * 当前页码 (从1开始)
     */
    private int pageNumber;

    /**
     * 当前页的起始记录位置 (从0开始)
     */
    private long pageStartPosition;

    /**
     * 当前处理的记录索引 (在当前页中的位置)
     */
    private int recordIndex;

    /**
     * 事务ID (用于分布式事务追踪)
     */
    private String transactionId;

    /**
     * 处理开始时间
     */
    private long startTime;

    /**
     * 处理结束时间
     */
    private long endTime;

    /**
     * 处理状态 (SUCCESS, FAILURE, PARTIAL)
     */
    private String processStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 业务上下文数据 (用于传递业务相关数据)
     */
    private java.util.Map<String, Object> businessContext;

    /**
     * 是否成功处理
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(processStatus);
    }

    /**
     * 是否处理失败
     */
    public boolean isFailure() {
        return "FAILURE".equals(processStatus);
    }

    /**
     * 是否部分成功
     */
    public boolean isPartial() {
        return "PARTIAL".equals(processStatus);
    }

    /**
     * 设置业务上下文数据
     */
    public void setBusinessData(String key, Object value) {
        if (businessContext == null) {
            businessContext = new java.util.HashMap<>();
        }
        businessContext.put(key, value);
    }

    /**
     * 获取业务上下文数据
     */
    public Object getBusinessData(String key) {
        return businessContext != null ? businessContext.get(key) : null;
    }

    /**
     * 获取处理耗时 (毫秒)
     */
    public long getDuration() {
        if (endTime > startTime) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 标记为成功
     */
    public void markSuccess() {
        this.processStatus = "SUCCESS";
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 标记为失败
     */
    public void markFailure(String errorMsg) {
        this.processStatus = "FAILURE";
        this.errorMessage = errorMsg;
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 标记为部分成功
     */
    public void markPartial() {
        this.processStatus = "PARTIAL";
        this.endTime = System.currentTimeMillis();
    }
}