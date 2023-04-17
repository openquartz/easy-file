package com.openquartz.easyfile.metrics.api.error;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import lombok.Getter;

/**
 * Metrics Error Code
 *
 * @author svnee
 */
@Getter
public enum MetricsErrorCode implements EasyFileErrorCode {

    METRICS_NAME_NOT_FOUND_ERROR("01",
        "Can not find metrics tracker manager with metrics name : {0} in metrics configuration."),
    ;

    private final String errorCode;
    private final String errorMsg;

    public static final String SIMPLE_BASE_CODE = "MetricsError-";

    MetricsErrorCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
    }


}
