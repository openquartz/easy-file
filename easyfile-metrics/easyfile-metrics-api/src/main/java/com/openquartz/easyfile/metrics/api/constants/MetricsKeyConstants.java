package com.openquartz.easyfile.metrics.api.constants;

/**
 * Metrics Key Constants
 *
 * @author svnee
 **/
public class MetricsKeyConstants {

    private MetricsKeyConstants() {
    }

    /**
     * total counter
     */
    public static final String DOWNLOAD_COUNTER = "easyfile.download.counter.total";

    /**
     * total sync download counter
     */
    public static final String SYNC_DOWNLOAD_COUNTER = "easyfile.download.counter.sync.total";

    /**
     * total async download counter
     */
    public static final String ASYNC_DOWNLOAD_COUNTER = "easyfile.download.counter.async.total";

    /**
     * sync timer
     */
    public static final String SYNC_DOWNLOAD_TIMER = "easyfile.download.sync.timer";


    /**
     * async count timer
     */
    public static final String ASYNC_INVOKE_DOWNLOAD_TIMER = "easyfile.download.invoke.async.timer";

    /**
     * async invoke timer
     */
    public static final String ASYNC_DOWNLOAD_TIMER = "easyfile.download.invoke.async.timer";


}
