package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFileLocalProperties
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileLocalProperties.PREFIX)
public class EasyFileLocalProperties {

    public static final String PREFIX = "easyfile.local";

    /**
     * 数据源连接配置
     */
    private EasyFileDataSourceProperties datasource;

    /**
     * 表配置
     */
    private EasyFileTableProperties table;

}
