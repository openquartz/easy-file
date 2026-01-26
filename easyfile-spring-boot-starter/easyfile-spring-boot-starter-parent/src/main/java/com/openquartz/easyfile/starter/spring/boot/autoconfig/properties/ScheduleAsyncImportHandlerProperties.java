package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;

/**
 * Schedule Async Import Handler Properties
 *
 * @author svnee
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = ScheduleAsyncImportHandlerProperties.PREFIX)
public class ScheduleAsyncImportHandlerProperties implements IDatabaseAsyncHandlerProperty {

    public static final String PREFIX = "easyfile.schedule.async.import.handler";

    ///////////////////// ThreadPool //////////////////////

    /**
     * Thread Pool Name Prefix
     */
    private String threadPoolThreadPrefix = "ScheduleAsyncImportHandler";

    /**
     * Reaper Thread Pool Name Prefix
     */
    private String reaperThreadPoolThreadPrefix = "ReaperScheduleAsyncImportHandler";

    /**
     * Core Thread Pool Size
     */
    private Integer threadPoolCoreSize = 2;

    /**
     * Reaper Core Thread Pool Size
     */
    private Integer reaperTheadPoolCoreSize = 1;

    /**
     * Min Reaper Seconds
     * Process time: lookBackHours -> minReaperSeconds
     */
    private Integer minReaperSeconds = 3600;

    /**
     * Max Execute Timeout (Seconds)
     * If exceeded, considered failed and re-executed
     */
    private Integer maxExecuteTimeout = 1600;

    /**
     * Schedule Period (Seconds)
     */
    private Integer schedulePeriod = 10;

    /**
     * Trigger Offset (Number of items per trigger)
     */
    private Integer triggerOffset = 50;

    /**
     * Look Back Hours
     */
    private Integer lookBackHours = 2;

    /**
     * Max Retry Count
     */
    private Integer maxTriggerCount = 5;

    /**
     * Max Archive Hours
     */
    private Integer maxArchiveHours = 24;

}
