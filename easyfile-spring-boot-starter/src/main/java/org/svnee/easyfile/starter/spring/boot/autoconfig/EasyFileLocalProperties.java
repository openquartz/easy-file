package org.svnee.easyfile.starter.spring.boot.autoconfig;

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

    public static final String PREFIX = "easyfile.local.datasource";

    /**
     * type
     */
    private String type;

    /**
     * jdbcUrl
     */
    private String url;

    /**
     * driver-class-name
     */
    private String driverClassName;

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;


}
