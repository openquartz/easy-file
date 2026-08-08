package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import lombok.Data;

/**
 * 刷新导入执行进度请求
 *
 * @author svnee
 */
@Data
public class RefreshImportExecuteProcessRequest {

    private Long registerId;

    private Integer executeProcess;

    private Long resumePosition;

    private ImportStatusEnum nextStatus;
}
