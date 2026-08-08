package com.openquartz.easyfile.common.request;

import lombok.Data;

/**
 * 导入字段更新请求
 *
 * @author svnee
 */
@Data
public class ImportUpdateRequest {

    private Long registerId;

    private Long totalCount;

    private String fileHash;

    private String resultFileUrl;
}
