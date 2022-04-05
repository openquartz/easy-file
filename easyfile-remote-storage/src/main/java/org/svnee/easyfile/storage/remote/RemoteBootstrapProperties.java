package org.svnee.easyfile.storage.remote;

import lombok.Data;

/**
 * Bootstrap properties.
 *
 * @author svnee
 * @date 2021/6/22 09:14
 */
@Data
public class RemoteBootstrapProperties {

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
