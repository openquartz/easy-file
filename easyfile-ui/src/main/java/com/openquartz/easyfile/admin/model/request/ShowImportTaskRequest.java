package com.openquartz.easyfile.admin.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * show import task request
 *
 * @author svnee
 */
@Data
public class ShowImportTaskRequest {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startImportTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endImportTime;

    private String importStatus;

    @NotNull(message = "分页页码不能为空")
    @JsonAlias("page")
    private Integer pageNum;

    @NotNull(message = "分页页面大小不能为空")
    @JsonAlias("limit")
    private Integer pageSize;
}
