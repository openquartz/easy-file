package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

/**
 * 异步下载触发类型
 *
 * @author svnee
 */
public enum DownloadAsyncTriggerType {

    DEFAULT,
    SCHEDULE,
    ROCKETMQ,
    DISRUPTOR;

}
