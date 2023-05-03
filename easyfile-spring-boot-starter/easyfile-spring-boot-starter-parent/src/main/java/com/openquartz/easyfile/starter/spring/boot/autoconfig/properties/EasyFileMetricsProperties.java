package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Metrics Property
 *
 * @author svnee
 */
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileMetricsProperties.PREFIX)
public class EasyFileMetricsProperties {

    public static final String PREFIX = "easyfile.metrics";

    /**
     * metrics-name
     */
    private String name;

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
