package com.openquartz.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RetryDownloadRequest {

    @NotNull(message = "注册ID不能为空")
    private Long registerId;

    /**
     * Optional operator for remote claim; blank falls back to system on the server.
     */
    private String operator;

    public static RetryDownloadRequest create(Long registerId) {
        RetryDownloadRequest request = new RetryDownloadRequest();
        request.setRegisterId(registerId);
        return request;
    }

    public static RetryDownloadRequest create(Long registerId, String operator) {
        RetryDownloadRequest request = create(registerId);
        request.setOperator(operator);
        return request;
    }
}
