package com.openquartz.easyfile.common.request;

import lombok.Data;

/**
 * 导入操作请求（重试/撤销）
 *
 * @author svnee
 */
@Data
public class ImportOperatorRequest {

    private Long registerId;

    private String operator;
}
