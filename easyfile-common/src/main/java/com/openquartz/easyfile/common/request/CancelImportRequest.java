package com.openquartz.easyfile.common.request;

import lombok.Data;

/**
 * 撤销导入请求
 *
 * @author svnee
 */
@Data
public class CancelImportRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 撤销人ID
     */
    private String cancelBy;

}
