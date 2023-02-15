package org.svnee.easyfile.admin.model;

import lombok.Data;

/**
 * EasyFileUser
 *
 * @author svnee
 * @since 1.2.0
 **/
@Data
public class EasyFileAdminUser {

    private String username;
    private String password;

    public EasyFileAdminUser() {
    }

    public EasyFileAdminUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
