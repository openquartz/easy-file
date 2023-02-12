package org.svnee.easyfile.admin.model;

import lombok.Data;

/**
 * EasyFileUser
 * @author svnee
 **/
@Data
public class EasyFileUser {

    private String username;
    private String password;

    public EasyFileUser() {
    }

    public EasyFileUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
