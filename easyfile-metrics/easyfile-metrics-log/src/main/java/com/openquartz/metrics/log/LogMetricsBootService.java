package com.openquartz.metrics.log;

import com.openquartz.easyfile.common.spi.SPI;
import com.openquartz.easyfile.metrics.api.config.MetricsConfig;
import com.openquartz.easyfile.metrics.api.spi.MetricsBootService;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import lombok.extern.slf4j.Slf4j;

/**
 * LogMetrics
 *
 * @author svnee
 */
@Slf4j
@SPI("log")
public class LogMetricsBootService implements MetricsBootService {
    
    @Override
    public void start(MetricsConfig metricsConfig, MetricsRegister register) {
        // TODO: 2023/4/16  
    }

    @Override
    public void stop() {
        // TODO: 2023/4/16  
    }
}
