package com.openquartz.easyfile.storage.download;

import java.util.List;
import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;

/**
 * 异步下载触发服务
 *
 * @author svnee
 */
public interface DownloadTriggerService {

    /**
     * 执行下载请求
     *
     * @param request 请求
     */
    void trigger(DownloadTriggerRequest request);

    /**
     * 需要触发注册ID
     *
     * @param lookBackHours 回溯时间
     * @param triggerOffset 触发偏移量
     * @param maxTriggerCount 最大触发次数
     * @return 注册ID
     */
    List<DownloadTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset);

    /**
     * 需要触发注册ID
     *
     * @param lookBackHours 回溯时间
     * @param triggerOffset 触发偏移量
     * @param minReaperSeconds 最小收割时间
     * @param maxTriggerCount 最大触发次数
     * @return 注册ID
     */
    List<DownloadTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer minReaperSeconds, Integer triggerOffset);

    /**
     * 需要触发注册ID
     *
     * @param lookBackHours 回溯时间
     * @param triggerOffset 触发偏移量
     * @param maxTriggerCount 最大触发次数
     * @return 注册ID
     */
    List<DownloadTriggerEntry> getTriggerCompensate(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset);

    /**
     * 根据registerId 查询DownloadTriggerEntry
     *
     * @param registerId registerId
     * @param maxTriggerCount 最大触发次数
     * @return entry
     */
    DownloadTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount);

    /**
     * 开始执行
     *
     * @param registerId 注册ID
     * @param triggerCount 原有的触发数
     * @return 是否成功
     */
    boolean startExecute(Long registerId, Integer triggerCount);

    /**
     * 执行成功
     *
     * @param registerId 注册ID
     */
    void exeSuccess(Long registerId);

    /**
     * 执行失败
     *
     * @param registerId 注册ID
     */
    void exeFail(Long registerId);

    /**
     * 进入排队
     *
     * @param registerId 注册ID
     */
    void enterWaiting(Long registerId);

    /**
     * 最大过期时间
     *
     * @param maxExpireSeconds 过期时间
     */
    void handleExpirationTrigger(Integer maxExpireSeconds);

    /**
     * 触发最大等待过期时间处理
     *
     * @param maxWaitingSeconds 最大等待时间超时
     */
    void handleWaitingExpirationTrigger(Integer maxWaitingSeconds);

    /**
     * 多久归档
     *
     * @param archiveHours 归档时间
     * @param maxTriggerCount 最大触发次数
     */
    void archiveHistoryTrigger(Integer archiveHours, Integer maxTriggerCount);

}
