package org.svnee.easyfile.server.notify;

import lombok.Data;

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

}
