package com.openquartz.easyfile.metrics.micrometer;

import com.openquartz.easyfile.common.spi.SPI;
import com.openquartz.easyfile.metrics.api.config.MetricsConfig;
import com.openquartz.easyfile.metrics.api.spi.MetricsBootService;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;

/**
 * Micrometer Metrics Boot Service
 *
 * @author svnee
 */
@SPI("micrometer")
public class MicrometerMetricsBootService implements MetricsBootService {

    @Override
    public void start(MetricsConfig metricsConfig, MetricsRegister register) {
        // TODO: 2023/4/16
    }

    @Override
    public void stop() {
        // TODO: 2023/4/16
    }
}
