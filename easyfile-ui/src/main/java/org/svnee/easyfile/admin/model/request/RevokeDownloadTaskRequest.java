package org.svnee.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * revoke download-task
 *
 * @author svnee
 **/
@Data
public class RevokeDownloadTaskRequest {

    @NotNull
    private Long registerId;

}
