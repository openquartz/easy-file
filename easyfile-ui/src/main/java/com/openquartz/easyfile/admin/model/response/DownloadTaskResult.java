package com.openquartz.easyfile.admin.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import lombok.Data;
import com.openquartz.easyfile.common.serdes.Long2StringSerializer;

/**
 * DownloadTask Result
 *
 * @author svnee
 **/
@Data
public class DownloadTaskResult {

    /**
     * 注册ID
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long registerId;

    /**
     * 上传状态
     */
    private String uploadStatus;

    /**
     * upload status desc
     */
    private String uploadStatusDesc;

    /**
     * 异常Msg
     */
    private String errorMsg;

    /**
     * 下载码
     */
    private String downloadCode;

    /**
     * 下载Desc
     */
    private String downloadCodeDesc;

    /**
     * 下载操作人
     */
    private String downloadOperateBy;

    /**
     * 下载操作人
     */
    private String downloadOperateName;

    /**
     * 下载时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date exportTime;

    /**
     * 最新执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastExecuteTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidTime;

    /**
     * 下载次数
     */
    private Integer downloadNum;

    /**
     * 执行进度
     */
    private Integer executeProcess;

    /**
     * 更新人
     */
    private String updateBy;

}
