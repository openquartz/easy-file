package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.storage.local.entity.AsyncImportTask;
import java.util.List;

/**
 * 异步导入任务Mapper
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
     * 根据导入code 查询异步导入任务
     *
     * @param taskCode 导入编码
     * @param appId    系统ID
     * @return 导入任务
     */
    AsyncImportTask selectByTaskCode(String taskCode, String appId);

    /**
     * 刷新任务描述
     *
     * @param id       ID
     * @param taskDesc 任务描述
     * @return affect row
     */
    int refreshTaskDesc(Long id, String taskDesc);
}
