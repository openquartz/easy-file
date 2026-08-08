package com.openquartz.easyfile.storage.local.convertor;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;

/**
 * 导入记录转换器
 *
 * @author svnee
 */
public final class AsyncImportRecordConverter {

    private AsyncImportRecordConverter() {
    }

    public static ImportResult convert(AsyncImportRecord importRecord) {
        boolean success = importRecord.getImportStatus() == ImportStatusEnum.SUCCESS
            || importRecord.getImportStatus() == ImportStatusEnum.PARTIAL_SUCCESS;
        return ImportResult.builder()
            .registerId(importRecord.getId())
            .success(success)
            .status(importRecord.getImportStatus() == null ? StringUtils.EMPTY
                : importRecord.getImportStatus().getCode())
            .process(importRecord.getExecuteProcess())
            .totalCount(importRecord.getTotalCount())
            .successCount(importRecord.getSuccessCount())
            .failCount(importRecord.getFailCount())
            .resultFileUrl(importRecord.getResultFileUrl())
            .errorMsg(importRecord.getErrorMsg())
            .startTime(importRecord.getCreateTime())
            .endTime(importRecord.getLastExecuteTime())
            .build();
    }
}
