package org.svnee.easyfile.admin.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * show download task request
 *
 * @author svnee
 * @since 1.2.0
 **/
@Data
public class ShowDownloadTaskRequest {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDownloadTime;

    /**
     * 下载时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDownloadTime;

    /**
     * 分页页码
     */
    @NotNull(message = "分页页码不能为空")
    @JsonAlias("page")
    private Integer pageNum;

    /**
     * 页面大小
     */
    @NotNull(message = "分页页面大小不能为空")
    @JsonAlias("limit")
    private Integer pageSize;

}
