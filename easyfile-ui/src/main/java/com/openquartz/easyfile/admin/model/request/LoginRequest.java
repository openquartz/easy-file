package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request
 *
 * @author svnee
 * @since 1.2.0
 **/
@Data
public class LoginRequest {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String rememberMe;

}
