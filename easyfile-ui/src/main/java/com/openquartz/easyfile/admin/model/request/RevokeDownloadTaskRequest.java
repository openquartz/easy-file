package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * revoke download-task
 *
 * @author svnee
 * @since 1.2.0
 **/
@Data
public class RevokeDownloadTaskRequest {

    @NotNull
    private Long registerId;

}
