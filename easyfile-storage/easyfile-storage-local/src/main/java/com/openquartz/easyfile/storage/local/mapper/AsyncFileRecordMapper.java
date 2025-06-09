package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.storage.local.mapper.condition.BaseRecordQueryCondition;
import com.openquartz.easyfile.storage.local.mapper.condition.UploadInfoChangeCondition;
import java.util.List;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncFileRecord;

/**
 * 异步下载记录Mapper
 *
 * @author svnee
 */
public interface AsyncFileRecordMapper {

    /**
     * 插入数据
     *
     * @param asyncFileRecord 插入记录
     * @return 插入条数
     */
    int insertSelective(AsyncFileRecord asyncFileRecord);

    /**
     * 根据ID 查询
     *
     * @param id id
     * @return record
     */
    AsyncFileRecord findById(Long id);

    /**
     * 变更上传信息
     *
     * @param condition 变更信息条件
     * @return 变更行数
     */
    int changeUploadInfo(UploadInfoChangeCondition condition);

    /**
     * 刷新上传状态
     *
     * @param id ID
     * @param oriUploadStatus oriUploadStatus
     * @param tagUploadStatus tagUploadStatus
     * @param updateBy 更新人
     * @return affect num
     */
    int refreshUploadStatus(Long id, HandleStatusEnum oriUploadStatus, HandleStatusEnum tagUploadStatus,
        String updateBy);

    /**
     * 下载
     *
     * @param id ID
     * @param uploadStatus 上传状态
     * @return affect row
     */
    int download(Long id, HandleStatusEnum uploadStatus);

    /**
     * 根据下载任务查询异步下载记录
     *
     * @param downloadTaskId 下载任务ID
     * @param uploadStatus 上传状态
     * @param offset 偏移量
     * @return 异步导出下载记录
     */
    List<AsyncFileRecord> listByTaskIdAndStatus(Long downloadTaskId, HandleStatusEnum uploadStatus, Integer offset);

    /**
     * 刷新执行进度
     *
     * @param registerId 注册ID
     * @param executeProcess 执行进度
     * @param nextStatus 下一个状态
     * @return affect row
     */
    int refreshExecuteProcess(Long registerId, Integer executeProcess, HandleStatusEnum nextStatus);

    /**
     * 重置执行进度
     *
     * @param registerId 注册ID
     * @return affect row
     */
    int resetExecuteProcess(Long registerId);

    /**
     * get By Condition
     *
     * @param condition condition
     * @return download record
     */
    List<AsyncFileRecord> selectByCondition(BaseRecordQueryCondition condition);

    /**
     * count By Condition
     *
     * @param condition condition
     * @return total rows
     */
    int countByCondition(BaseRecordQueryCondition condition);

    /**
     * get registerId locale
     *
     * @param registerId registerId
     * @return locale
     */
    String getLocale(Long registerId);
}
