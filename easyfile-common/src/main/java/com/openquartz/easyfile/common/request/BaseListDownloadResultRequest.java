package com.openquartz.easyfile.common.request;

import java.util.Date;
import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class BaseListDownloadResultRequest {

    /**
     * 下载编码
     */
    private String downloadCode;

    /**
     * 下载人
     */
    private String downloadOperateBy;

    /**
     * 开始时间
     */
    private Date startDownloadTime;

    /**
     * 下载时间
     */
    private Date endDownloadTime;

    /**
     * 分页页码
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;
}
