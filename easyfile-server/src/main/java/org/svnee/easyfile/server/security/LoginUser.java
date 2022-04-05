package org.svnee.easyfile.server.security;

import lombok.Data;

/**
 * Login user.
 *
 * @author svnee
 */
@Data
public class LoginUser {

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * rememberMe
     */
    private Integer rememberMe;

}
