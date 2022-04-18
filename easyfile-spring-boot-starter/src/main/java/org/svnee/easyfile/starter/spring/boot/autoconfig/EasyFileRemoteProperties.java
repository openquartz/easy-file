package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调用远程服务配置
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileRemoteProperties.PREFIX)
public class EasyFileRemoteProperties {

    public static final String PREFIX = "easyfile.remote";

    /**
     * Username.
     */
    private String username;

    /**
     * Password.
     */
    private String password;

    /**
     * Server addr
     */
    private String serverAddr;

    /**
     * Namespace
     */
    private String namespace;

}
