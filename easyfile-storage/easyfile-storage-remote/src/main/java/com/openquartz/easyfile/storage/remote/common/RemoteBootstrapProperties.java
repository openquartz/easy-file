package com.openquartz.easyfile.storage.remote.common;

import lombok.Data;

/**
 * Bootstrap properties.
 *
 * @author svnee
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

}
