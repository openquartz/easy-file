package com.openquartz.easyfile.common.request;

import lombok.Data;

/**
 * 导入限流请求
 *
 * @author svnee
 */
@Data
public class ImportLimitingRequest {

    /**
     * 服务ID
     */
    private String appId;

    /**
     * 导入编码
     */
    private String importCode;

    /**
     * 操作人
     */
    private String operator;

}
