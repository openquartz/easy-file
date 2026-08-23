package com.openquartz.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RetryDownloadRequest {

    @NotNull(message = "注册ID不能为空")
    private Long registerId;

    public static RetryDownloadRequest create(Long registerId) {
        RetryDownloadRequest request = new RetryDownloadRequest();
        request.setRegisterId(registerId);
        return request;
    }
}
