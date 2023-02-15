package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调用远程服务配置
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileRemoteProperties.PREFIX)
public class EasyFileRemoteProperties {

    public static final String PREFIX = "easyfile.remote";

    /**
     * Username.
     * username
     */
    private String username;

    /**
     * Password.
     * password
     */
    private String password;

    /**
     * Server addr
     * server-addr
     */
    private String serverAddr;

    /**
     * Namespace
     * namespace
     */
    private String namespace;

}
