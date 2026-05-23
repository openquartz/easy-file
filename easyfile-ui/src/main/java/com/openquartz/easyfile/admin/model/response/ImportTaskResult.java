package com.openquartz.easyfile.admin.model.response;

import java.util.Date;
import lombok.Data;

/**
 * import task result
 *
 * @author svnee
 */
@Data
public class ImportTaskResult {

    private Long registerId;

    private String importStatus;

    private String importStatusDesc;

    private String importCode;

    private String importCodeDesc;

    private String createBy;

    private Date createTime;

    private Date lastExecuteTime;

    private Integer executeProcess;

    private Long totalCount;

    private Long successCount;

    private Long failCount;

    private String resultFileUrl;

    private String errorMsg;

    private Boolean allowRetry;

    private Boolean allowRevoke;
}
