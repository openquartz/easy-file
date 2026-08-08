package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.storage.local.entity.AsyncImportAppEntity;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTask;
import java.util.List;

/**
 * 异步导入任务 Mapper
 *
 * @author svnee
 */
public interface AsyncImportTaskMapper {

    /**
     * 插入数据
     *
     * @param asyncImportTask 异步导入任务
     * @return 插入条数
     */
    int insertSelective(AsyncImportTask asyncImportTask);

    /**
     * 根据导入 code 查询异步导入任务
     *
     * @param taskCode 导入编码
     * @param appId 系统 ID
     * @return 导入任务
     */
    AsyncImportTask selectByImportCode(String taskCode, String appId);

    /**
     * 批量查询
     *
     * @param importCodeList 导入 code
     * @param appIdList appID
     * @return 导入任务
     */
    List<AsyncImportTask> listByImportCode(List<String> importCodeList, List<String> appIdList);

    /**
     * 刷新任务描述
     *
     * @param id ID
     * @param taskDesc 任务描述
     * @return affect row
     */
    int refreshTaskDesc(Long id, String taskDesc);

    /**
     * get same unifiedAppId appId
     *
     * @param unifiedAppId unifiedAppId
     * @return appId
     */
    List<String> getByUnifiedAppId(String unifiedAppId);

    /**
     * get async import app entity
     *
     * @return import entity
     */
    List<AsyncImportAppEntity> getAsyncImportAppEntity();

}
