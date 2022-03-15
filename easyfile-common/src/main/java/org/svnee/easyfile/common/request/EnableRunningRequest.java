package org.svnee.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校验运行请求
 *
 * @author svnee
 */
@Data
public class EnableRunningRequest {

    /**
     * 注册ID
     */
    @NotNull(message = "注册ID不能为空")
    private Long registerId;

    public static EnableRunningRequest create(Long registerId) {
        EnableRunningRequest request = new EnableRunningRequest();
        request.setRegisterId(registerId);
        return request;
    }
}
