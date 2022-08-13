package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DisruptorAsyncHandlerProperties
 *
 * @author svnee
 **/
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = DisruptorAsyncHandlerProperties.PREFIX)
public class DisruptorAsyncHandlerProperties implements MqAsyncHandlerProperties {

    public static final String PREFIX = "easyfile.disruptor.async.download.handler";

    /**
     * rightBufferSize
     */
    private Integer ringBufferSize = 64;

    /**
     * 线程池前缀
     */
    private String threadPoolThreadPrefix = "DisruptorAsyncHandler";

    /**
     * 回溯时间
     * 单位：小时
     */
    private Integer lookBackHours = 2;

    /**
     * 最大重试次数
     */
    private Integer maxTriggerCount = 5;

    /**
     * 最大归档小时
     */
    private Integer maxArchiveHours = 24;

    /**
     * 最大超时时间
     */
    private Integer maxExecuteTimeout = 1600;

    /**
     * 调度周期
     * 单位：秒
     */
    private Integer schedulePeriod = 10;

}
