package com.openquartz.easyfile.server.notify;

import java.util.Date;
import lombok.Data;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;

/**
 * 通知消息模版
 *
 * @author svnee
 */
@Data
public class NotifyMessageTemplate {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

    /**
     * 下载时间
     */
    private Date downloadTime;

    /**
     * 下载完成时间
     */
    private Date downloadFinishedTime;

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 失败信息
     */
    private String errorMsg;

    /**
     * 上传状态
     */
    private HandleStatusEnum uploadStatus;
}
