package org.svnee.easyfile.starter.executor.bean;


import lombok.Data;

/**
 * 触发执行消息
 *
 * @author svnee
 **/
@Data
public class DownloadTriggerMessage {

    /**
     * registerId
     */
    private Long registerId;

    /**
     * 发送时间
     */
    private Long sendTimestamp;

}
