package com.openquartz.easyfile.common.response;

import lombok.Builder;
import lombok.Data;

/**
 * 导入错误列信息
 *
 * @author svnee
 */
@Data
@Builder
public class ErrorColumnInfo {

    private Integer rowNumber;

    private String rowKey;

    private String columnName;

    private String errorValue;

    private String errorMessage;

    private String suggestedAction;
}
