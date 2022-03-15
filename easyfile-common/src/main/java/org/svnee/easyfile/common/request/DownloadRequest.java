package org.svnee.easyfile.common.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下载次数
 *
 * @author svnee
 */
@Data
public class DownloadRequest {

    @NotBlank(message = "AppId不能为空")
    private String appId;

    @NotNull(message = "注册Id不能为空")
    private Long registerId;

    private String downloadOperateBy;

    private String downloadOperateName;

}
