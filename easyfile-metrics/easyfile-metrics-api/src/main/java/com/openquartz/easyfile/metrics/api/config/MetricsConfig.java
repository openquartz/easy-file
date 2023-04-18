package com.openquartz.easyfile.metrics.api.config;

import com.openquartz.easyfile.common.spi.SPI;
import lombok.Data;

import java.util.Properties;

/**
 * Metrics config.
 *
 * @author svnee
 */
@Data
@SPI("MetricsConfig")
public final class MetricsConfig {

    /**
     * metrics-name
     */
    private String metricsName;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private Integer port = 9091;

    /**
     * JMX config
     */
    private String jmxConfig;

    /**
     * properties
     */
    private Properties props;
}

