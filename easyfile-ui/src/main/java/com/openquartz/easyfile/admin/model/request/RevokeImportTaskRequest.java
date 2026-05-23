package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * revoke import task request
 *
 * @author svnee
 */
@Data
public class RevokeImportTaskRequest {

    @NotNull(message = "导入ID不能为空")
    private Long registerId;
}
