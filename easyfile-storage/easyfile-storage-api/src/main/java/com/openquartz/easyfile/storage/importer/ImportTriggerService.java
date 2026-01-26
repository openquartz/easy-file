package com.openquartz.easyfile.storage.importer;

import java.util.List;
import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;

/**
 * 异步导入触发服务
 *
 * @author svnee
 */
public interface ImportTriggerService {

    /**
     * 执行导入请求
     *
     * @param request 请求
     */
    void trigger(ImportTriggerRequest request);

    /**
     * 需要触发注册ID
     *
     * @param lookBackHours 回溯时间
     * @param triggerOffset 触发偏移量
     * @param maxTriggerCount 最大触发次数
     * @return 注册ID
     */
    List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
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
    List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer minReaperSeconds, Integer triggerOffset);

    /**
     * 需要触发注册ID
     *
     * @param lookBackHours 回溯时间
     * @param triggerOffset 触发偏移量
     * @param maxTriggerCount 最大触发次数
     * @return 注册ID
     */
    List<ImportTriggerEntry> getTriggerCompensate(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset);

    /**
     * 根据registerId 查询ImportTriggerEntry
     *
     * @param registerId registerId
     * @param maxTriggerCount 最大触发次数
     * @return entry
     */
    ImportTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount);

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
     * 多久归档
     *
     * @param archiveHours 归档时间
     * @param maxTriggerCount 最大触发次数
     */
    void archiveHistoryTrigger(Integer archiveHours, Integer maxTriggerCount);
}
