package org.svnee.easyfile.server.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.server.entity.AsyncDownloadRecord;
import org.svnee.easyfile.server.mapper.condition.BaseRecordQueryCondition;
import org.svnee.easyfile.server.mapper.condition.LimiterRecordQueryCondition;
import org.svnee.easyfile.server.mapper.condition.RetryQueryCondition;
import org.svnee.easyfile.server.mapper.condition.UploadInfoChangeCondition;

/**
 * 异步下载记录Mapper
 *
 * @author svnee
 */
public interface AsyncDownloadRecordMapper {

    /**
     * 批量插入异步下载记录
     *
     * @param list 数据集合
     * @return 插入条数
     */
    int insertList(@Param("list") List<AsyncDownloadRecord> list);

    /**
     * 插入数据
     *
     * @param asyncDownloadRecord 插入记录
     * @return 插入条数
     */
    int insertSelective(AsyncDownloadRecord asyncDownloadRecord);

    /**
     * 根据ID 查询
     *
     * @param id id
     * @return record
     */
    AsyncDownloadRecord findById(@Param("id") Long id);

    /**
     * 变更上传信息
     *
     * @param condition 变更信息条件
     * @return 变更行数
     */
    int changeUploadInfo(@Param("condition") UploadInfoChangeCondition condition);

    /**
     * 查询最大创建时间
     *
     * @param condition 条件
     * @return 条数
     */
    Integer countTimeRange(@Param("condition") LimiterRecordQueryCondition condition);

    /**
     * 查询导出重试记录
     *
     * @param condition 查询条件
     * @return 下载记录
     */
    List<AsyncDownloadRecord> selectExportRecordOfRetryable(@Param("condition") RetryQueryCondition condition);

    /**
     * 更新重试信息
     *
     * @param id 主键
     * @return affect rows
     */
    int updateRetryById(@Param("id") Long id);

    /**
     * 更新重试执行异常
     *
     * @param id ID
     * @param errorMsg 异常信息
     * @param failStatusList 失败状态条件
     */
    void updateExecuteErrorMsg(@Param("id") Long id, @Param("errorMsg") String errorMsg,
        @Param("list") List<UploadStatusEnum> failStatusList);

    /**
     * 根据条件查询
     *
     * @param condition 查询条件
     * @return 导出结果
     */
    List<AsyncDownloadRecord> selectByCondition(@Param("condition") BaseRecordQueryCondition condition);

    /**
     * 刷新上传状态
     *
     * @param id ID
     * @param oriUploadStatus oriUploadStatus
     * @param tagUploadStatus tagUploadStatus
     * @param updateBy 更新人
     * @return affect num
     */
    int refreshUploadStatus(@Param("id") Long id,
        @Param("oriUploadStatus") UploadStatusEnum oriUploadStatus,
        @Param("tagUploadStatus") UploadStatusEnum tagUploadStatus,
        @Param("updateBy") String updateBy);

    /**
     * 下载
     *
     * @param id ID
     * @param uploadStatus 上传状态
     */
    void download(@Param("id") Long id,
        @Param("uploadStatus") UploadStatusEnum uploadStatus);

    /**
     * 根据taskId和状态查询导出记录
     *
     * @param downloadTaskId 下载任务ID
     * @param uploadStatus 导出状态
     * @param offset 偏移量
     * @return 导出结果
     */
    List<AsyncDownloadRecord> listByTaskIdAndStatus(@Param("downloadTaskId") Long downloadTaskId,
        @Param("uploadStatus") UploadStatusEnum uploadStatus, @Param("offset") int offset);

    /**
     * 重置执行进度
     *
     * @param id ID
     * @return affect
     */
    int resetExecuteProcess(@Param("id") Long id);

    /**
     * 刷新执行进度
     *
     * @param id id
     * @param executeProcess 执行进度
     * @return affect row
     */
    int refreshExecuteProcess(@Param("id") Long id, @Param("executeProcess") Integer executeProcess);
}
