package org.svnee.easyfile.starter.executor.trigger;

import lombok.Data;

/**
 * 下载触发器消息
 *
 * @author svnee
 **/
@Data
public class DownloadTriggerMessage {

    private Long registerId;

    private Long triggerTimestamp;

}
