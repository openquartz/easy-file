package com.openquartz.easyfile.core.executor.trigger;

import lombok.Data;

/**
 * 下载触发器消息
 *
 * @author svnee
 **/
@Data
public class TriggerMessage {

    private Long registerId;

    private Long triggerTimestamp;

}
