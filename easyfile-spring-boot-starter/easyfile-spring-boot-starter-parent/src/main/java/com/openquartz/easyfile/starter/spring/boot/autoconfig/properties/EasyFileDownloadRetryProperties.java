package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileDownloadRetryProperties.PREFIX)
public class EasyFileDownloadRetryProperties {

    public static final String PREFIX = "easyfile.download.retry";

    private boolean scanEnabled = true;
    private int scanPeriodSeconds = 10;
    private int orphanGraceSeconds = 30;
    private int lookBackHours = 24;
    private int scanBatchSize = 50;
}
