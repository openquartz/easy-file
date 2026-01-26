package com.openquartz.easyfile.storage.local.mapper;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;

/**
 * 异步导入记录Mapper
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
     * 根据ID 查询
     *
     * @param id id
     * @return record
     */
    AsyncImportRecord findById(Long id);

    /**
     * 刷新上传状态
     *
     * @param id ID
     * @param uploadStatus 上传状态
     * @param updateBy 更新人
     * @return affect num
     */
    int updateUploadStatus(Long id, UploadStatusEnum uploadStatus, String updateBy);
    
    /**
     * 刷新导入结果
     *
     * @param id ID
     * @param uploadStatus 状态
     * @param errorFileUrl 错误文件地址
     * @param successRows 成功行
     * @param failRows 失败行
     * @param totalRows 总行
     * @return affect num
     */
    int updateImportResult(Long id, UploadStatusEnum uploadStatus, String errorFileUrl, int successRows, int failRows, int totalRows);
}
