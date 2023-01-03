package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class EasyFileDataSourceProperties {

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
