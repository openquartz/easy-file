package org.svnee.easyfile.server.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFileAdminProperty
 *
 * @author svnee
 * @since 1.2.0
 **/
@ConfigurationProperties(prefix = EasyFileUiProperties.PREFIX)
public class EasyFileUiProperties {

    public static final String PREFIX = "easyfile.ui";

    /**
     * admin account
     */
    private AdminUser admin = new AdminUser();

    public static class AdminUser {

        private String username = "admin";
        private String password = "admin";

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public AdminUser getAdmin() {
        return admin;
    }

    public void setAdmin(AdminUser admin) {
        this.admin = admin;
    }
}
