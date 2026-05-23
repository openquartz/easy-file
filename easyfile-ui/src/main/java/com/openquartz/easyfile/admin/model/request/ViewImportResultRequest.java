package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * view import result request
 *
 * @author svnee
 */
@Data
public class ViewImportResultRequest {

    @NotNull(message = "导入ID不能为空")
    private Long registerId;
}
