package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定时调度异步处理器配置
 *
 * @author svnee
 **/
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = ScheduleAsyncHandlerProperties.PREFIX)
public class ScheduleAsyncHandlerProperties implements DatabaseAsyncHandlerProperties {

    public static final String PREFIX = "easyfile.schedule.async.download.handler";

    ///////////////////// ThreadPool //////////////////////

    /**
     * 线程名前缀
     */
    private String threadPoolThreadPrefix = "ScheduleAsyncHandler";

    /**
     * reaper-线程调度前缀
     */
    private String reaperThreadPoolThreadPrefix = "ReaperScheduleAsyncHandler";

    /**
     * 核心线程数
     * core-pool-size
     */
    private Integer threadPoolCoreSize = 2;

    /**
     * reaper-线程池核心数
     */
    private Integer reaperTheadPoolCoreSize = 1;

    /**
     * 最小收割时间
     * 处理时间为：lookBackHours->minReaperSeconds
     */
    private Integer minReaperSeconds = 3600;

    /**
     * 最大执行超时时间 单位秒
     * 超出此时间认定为执行失败。重新执行
     */
    private Integer maxExecuteTimeout = 1600;

    /**
     * 调度周期 单位：秒
     */
    private Integer schedulePeriod = 10;

    /**
     * 一次触发处理条数
     */
    private Integer triggerOffset = 50;

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

}
