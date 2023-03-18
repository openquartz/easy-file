package org.svnee.easyfile.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * 下载结果
 *
 * @author svnee
 */
@Data
public class DownloadResult extends ExportResult {

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
     * 失败信息
     */
    private String errorMsg;

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
