package com.openquartz.easyfile.server.mapper;

import com.openquartz.easyfile.server.entity.AsyncFileTask;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.openquartz.easyfile.server.entity.AsyncFileAppEntity;

/**
 * 异步下载任务Mapper
 *
 * @author svnee
 */
@Mapper
public interface AsyncFileTaskMapper {

    /**
     * 插入数据
     *
     * @param asyncFileTask 异步下载任务
     * @return 插入条数
     */
    int insertSelective(AsyncFileTask asyncFileTask);

    /**
     * 根据ID 主键插叙
     *
     * @param id ID 主键
     * @return 异步下载任务
     */
    AsyncFileTask findById(@Param("id") Long id);

    /**
     * 根据下载code 查询异步下载任务
     *
     * @param taskCode 下载编码
     * @param appId 系统ID
     * @return 下载任务
     */
    AsyncFileTask selectByDownloadCode(@Param("taskCode") String taskCode, @Param("appId") String appId);

    /**
     * 加载所有的AppId
     *
     * @return APP Id 集合
     */
    List<String> selectAllAppId();

    /**
     * 批量查询
     *
     * @param downloadCodeList 下载code
     * @param appIdList appID
     * @return 下载任务
     */
    List<AsyncFileTask> listByDownloadCode(@Param("list") List<String> downloadCodeList,
        @Param("appIdList") List<String> appIdList);

    /**
     * 根据统一的标识进行查询涉及到的APP ID
     *
     * @param unifiedAppId 统一APP ID标识
     * @return appId
     */
    List<String> getByUnifiedAppId(@Param("unifiedAppId") String unifiedAppId);

    /**
     * 批量插入
     *
     * @param list list
     */
    void insertList(@Param("list") List<AsyncFileTask> list);

    /**
     * 刷新任务描述
     *
     * @param id ID
     * @param taskDesc 任务描述
     */
    void refreshTaskDesc(@Param("id") Long id, @Param("taskDesc") String taskDesc);

    /**
     * get app tree
     *
     * @return app tree
     */
    List<AsyncFileAppEntity> getAppTree();

    /**
     * 获取当前任务的语言
     * @param registerId 注册ID
     * @return locale
     */
    String getLocale(@Param("registerId") Long registerId);
}
