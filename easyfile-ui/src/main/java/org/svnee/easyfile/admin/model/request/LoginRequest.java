package org.svnee.easyfile.admin.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request
 * @author svnee
 **/
@Data
public class LoginRequest {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String rememberMe;

}
