package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * retry import task request
 *
 * @author svnee
 */
@Data
public class RetryImportTaskRequest {

    @NotNull(message = "导入ID不能为空")
    private Long registerId;
}
