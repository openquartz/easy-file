package com.openquartz.easyfile.common.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 导出结果查询
 *
 * @author svnee
 */
@Data
public class ListDownloadResultRequest {

    @NotBlank
    private String unifiedAppId;

    /**
     * 限定 app Id
     */
    private List<String> limitedAppIdList;

    /**
     * 下载编码
     */
    private String downloadCode;

    /**
     * 下载人
     */
    private String downloadOperateBy;

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
    private Integer pageNum;

    /**
     * 页面大小
     */
    @NotNull(message = "分页页面大小不能为空")
    private Integer pageSize;

}
