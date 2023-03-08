package org.svnee.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重试执行
 *
 * @author svnee
 * @since 1.2.2
 **/
@Data
public class RetryDownloadTaskRequest {

    @NotNull
    private Long registerId;

}
