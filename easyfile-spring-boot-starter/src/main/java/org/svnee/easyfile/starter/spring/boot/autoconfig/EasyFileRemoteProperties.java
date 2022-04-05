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

    /**
     * Item id
     */
    private String itemId;

    /**
     * Whether to enable dynamic thread pool
     */
    private Boolean enable = true;

    /**
     * Print dynamic thread pool banner
     */
    private Boolean banner = true;

    /**
     * Enable client data collect
     */
    private Boolean enableCollect = true;

    /**
     * Task buffer container capacity
     */
    private Integer taskBufferSize = 4096;

    /**
     * Delay starting data acquisition task. unit: ms
     */
    private Long initialDelay = 10000L;

    /**
     * Time interval for client to collect monitoring data. unit: ms
     */
    private Long collectInterval = 5000L;

    /**
     * JSON serialization type.
     */
    private String jsonSerializeType = "JACKSON";

}
