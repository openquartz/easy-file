package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import lombok.Data;

/**
 * 导入完成请求
 *
 * @author svnee
 */
@Data
public class ImportCompleteRequest {

    private Long registerId;

    private ImportStatusEnum status;

    private Long totalCount;

    private Long successCount;

    private Long failCount;

    private Long resumePosition;

    private String errorMsg;
}
