package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.mapper.condition.ImportRecordQueryCondition;
import java.util.List;

/**
 * 异步导入记录 Mapper
 *
 * @author svnee
 */
public interface AsyncImportRecordMapper {

    /**
     * 插入数据
     *
     * @param asyncImportRecord 插入记录
     * @return 插入条数
     */
    int insertSelective(AsyncImportRecord asyncImportRecord);

    /**
     * 根据 ID 查询
     *
     * @param id id
     * @return record
     */
    AsyncImportRecord findById(Long id);

    /**
     * 刷新导入状态（CAS）
     *
     * @param id ID
     * @param oriImportStatus 原状态
     * @param tagImportStatus 目标状态
     * @param updateBy 更新人
     * @return affect num
     */
    int refreshImportStatus(Long id, ImportStatusEnum oriImportStatus, ImportStatusEnum tagImportStatus,
        String updateBy);

    /**
     * 刷新执行进度
     *
     * @param registerId 注册 ID
     * @param executeProcess 执行进度
     * @param resumePosition 断点位置
     * @param nextStatus 下一个状态
     * @return affect row
     */
    int refreshExecuteProcess(Long registerId, Integer executeProcess, Long resumePosition,
        ImportStatusEnum nextStatus);

    /**
     * 重置执行进度
     *
     * @param registerId 注册 ID
     * @return affect row
     */
    int resetExecuteProcess(Long registerId);

    /**
     * 更新总行数
     *
     * @param registerId 注册 ID
     * @param totalCount 总行数
     * @return affect row
     */
    int updateTotalCount(Long registerId, Long totalCount);

    /**
     * 更新文件哈希
     *
     * @param registerId 注册 ID
     * @param fileHash 文件哈希
     * @return affect row
     */
    int updateFileHash(Long registerId, String fileHash);

    /**
     * 更新结果文件地址
     *
     * @param registerId 注册 ID
     * @param resultFileUrl 结果文件地址
     * @return affect row
     */
    int updateResultFileUrl(Long registerId, String resultFileUrl);

    /**
     * 完成导入
     *
     * @param registerId 注册 ID
     * @param status 最终状态
     * @param totalCount 总行数
     * @param successCount 成功行数
     * @param failCount 失败行数
     * @param resumePosition 断点位置
     * @param errorMsg 错误信息
     * @return affect row
     */
    int complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
        Long failCount, Long resumePosition, String errorMsg);

    /**
     * get By Condition
     *
     * @param condition condition
     * @return import record
     */
    List<AsyncImportRecord> selectByCondition(ImportRecordQueryCondition condition);

    /**
     * count By Condition
     *
     * @param condition condition
     * @return total rows
     */
    int countByCondition(ImportRecordQueryCondition condition);

}
