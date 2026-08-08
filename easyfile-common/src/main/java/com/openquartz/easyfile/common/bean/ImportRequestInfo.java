package com.openquartz.easyfile.common.bean;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import lombok.Data;

/**
 * 导入请求Info（handler 恢复载荷）
 *
 * @author svnee
 */
@Data
public class ImportRequestInfo {

    private String importCode;

    private ImportRegisterRequest registerRequest;

    private Long resumePosition;

    private ImportStatusEnum importStatus;

}
