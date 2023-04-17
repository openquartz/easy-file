package com.openquartz.easyfile.metrics.api.metric;

import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.spi.ExtensionLoaderFactory;
import com.openquartz.easyfile.metrics.api.config.MetricsConfig;
import com.openquartz.easyfile.metrics.api.error.MetricsErrorCode;
import com.openquartz.easyfile.metrics.api.spi.MetricsBootService;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * Metrics tracker facade.
 *
 * @author svnee
 */
@Slf4j
public final class MetricsTrackerFacade implements AutoCloseable {

    private MetricsBootService metricsBootService;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Init for metrics tracker manager.
     *
     * @param metricsConfig metrics config
     */
    public void start(final MetricsConfig metricsConfig) {
        if (this.isStarted.compareAndSet(false, true)) {
            metricsBootService = ExtensionLoaderFactory.load(MetricsBootService.class, metricsConfig.getMetricsName());
            Asserts.notNull(metricsBootService, MetricsErrorCode.METRICS_NAME_NOT_FOUND_ERROR,
                metricsConfig.getMetricsName());
            metricsBootService.start(metricsConfig,
                ExtensionLoaderFactory.load(MetricsRegister.class, metricsConfig.getMetricsName()));
        } else {
            log.info("[MetricsTrackerFacade#start] metrics tracker has started !");
        }
    }

    @Override
    public void close() {
        this.isStarted.compareAndSet(true, false);
        if (null != metricsBootService) {
            metricsBootService.stop();
        }
    }
}

