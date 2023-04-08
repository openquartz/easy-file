package com.openquartz.easyfile.common.bean.auth;

import lombok.Data;

/**
 * 认证用户请求信息
 *
 * @author svnee
 **/
@Data
public class AuthUserRequest {

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;
}
