package com.openquartz.easyfile.storage.importer;

import com.openquartz.easyfile.common.bean.ImportRecordInfo;
import com.openquartz.easyfile.common.request.ImportCallbackRequest;
import com.openquartz.easyfile.common.request.RegisterImportRequest;

/**
 * 导入存储服务
 *
 * @author svnee
 */
public interface ImportStorageService {

    /**
     * 注册导入任务
     *
     * @param request 请求
     * @return 注册ID
     */
    Long register(RegisterImportRequest request);

    /**
     * 更新导入结果
     *
     * @param request 请求
     */
    void updateImportResult(ImportCallbackRequest request);

    /**
     * 校验是否可以运行
     *
     * @param registerId 注册ID
     * @return boolean
     */
    boolean enableRunning(Long registerId);

    /**
     * Get Import Record
     *
     * @param registerId registerId
     * @return ImportRecordInfo
     */
    ImportRecordInfo getImportRecord(Long registerId);
}
