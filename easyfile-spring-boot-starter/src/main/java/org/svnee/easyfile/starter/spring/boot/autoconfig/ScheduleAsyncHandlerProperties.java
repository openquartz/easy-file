package org.svnee.easyfile.starter.spring.boot.autoconfig;

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
public class ScheduleAsyncHandlerProperties {

    public static final String PREFIX = "easyfile.schedule.async.download.handler";

    ///////////////////// ThreadPool //////////////////////

    /**
     * 开启
     */
    private boolean enable = false;

    /**
     * 线程名前缀
     */
    private String threadPoolThreadPrefix = "ScheduleAsyncHandler";

    /**
     * 核心线程数
     * core-pool-size
     */
    private Integer threadPoolCorePoolSize = 10;

    /**
     * 最大执行超时时间 单位秒
     * 超出此时间认定为执行失败。重新执行
     */
    private Integer maxExecuteTimeout = 1600;

    /**
     * 调度周期 单位：秒
     */
    private Integer schedulePeriod = 500;

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

}
