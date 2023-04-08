package com.openquartz.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下载次数
 *
 * @author svnee
 */
@Data
public class DownloadRequest {

    @NotNull(message = "注册Id不能为空")
    private Long registerId;

    private String downloadOperateBy;

    private String downloadOperateName;

}
