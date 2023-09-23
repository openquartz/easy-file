package com.openquartz.easyfile.storage.local.mapper;

import java.util.List;
import com.openquartz.easyfile.storage.local.entity.AsyncFileAppEntity;
import com.openquartz.easyfile.storage.local.entity.AsyncFileTask;

/**
 * 异步下载任务Mapper
 *
 * @author svnee
 */
public interface AsyncFileTaskMapper {

    /**
     * 插入数据
     *
     * @param asyncFileTask 异步下载任务
     * @return 插入条数
     */
    int insertSelective(AsyncFileTask asyncFileTask);

    /**
     * 根据下载code 查询异步下载任务
     *
     * @param taskCode 下载编码
     * @param appId 系统ID
     * @return 下载任务
     */
    AsyncFileTask selectByTaskCode(String taskCode, String appId);

    /**
     * 批量查询
     *
     * @param downloadCodeList 下载code
     * @param appIdList appID
     * @return 下载任务
     */
    List<AsyncFileTask> listByDownloadCode(List<String> downloadCodeList, List<String> appIdList);

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
     * get async download entity
     *
     * @return download entity
     */
    List<AsyncFileAppEntity> getAsyncDownloadAppEntity();

}
