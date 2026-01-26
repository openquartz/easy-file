package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.storage.local.dictionary.DownloadTriggerStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTrigger;
import com.openquartz.easyfile.storage.local.mapper.condition.QueryImportTriggerCondition;
import java.util.List;

/**
 * AsyncImportTriggerMapper
 *
 * @author svnee
 */
public interface AsyncImportTriggerMapper {

    /**
     * 保存触发器
     *
     * @param trigger 触发器
     * @return 影响行数
     */
    int insert(AsyncImportTrigger trigger);

    /**
     * 刷新触发状态
     *
     * @param registerId ID
     * @param triggerStatus 触发器状态
     * @param delineateStatusList 圈定状态
     * @return 影响行数
     */
    int refreshStatus(Long registerId, DownloadTriggerStatusEnum triggerStatus,
        List<DownloadTriggerStatusEnum> delineateStatusList);

    /**
     * 执行
     *
     * @param registerId ID
     * @param triggerStatus 触发器状态
     * @param delineateStatusList 圈定状态
     * @param triggerCount 触发执行次数
     * @return 影响行数
     */
    int execute(Long registerId, DownloadTriggerStatusEnum triggerStatus,
        List<DownloadTriggerStatusEnum> delineateStatusList, Integer triggerCount);

    /**
     * 查询
     *
     * @param condition 查询条件
     * @return 触发次数
     */
    List<AsyncImportTrigger> select(QueryImportTriggerCondition condition);

    /**
     * 根据registerId 查询
     *
     * @param registerId registerId
     * @return 注册器
     */
    AsyncImportTrigger selectByRegisterId(Long registerId);

    /**
     * 根据
     *
     * @param id ID
     * @return id
     */
    int deleteById(Long id);

}
