package com.openquartz.easyfile.common.request;

import lombok.Builder;
import lombok.Data;

/**
 * 导入注册请求
 *
 * @author svnee
 */
@Data
@Builder
public class ImportRegisterRequest {

    private String appId;

    private String importCode;

    private String ossUrl;

    private String fileName;

    private String fileType;

    private String businessUniqueKey;

    private String executeParam;

    private String createBy;

    private Boolean enableAsync;

    private Boolean supportResumable;

    private Integer pageSize;

    private Integer maxRetry;

    private String notifyEmail;

    private String locale;

    private Boolean enableNotify;
}
