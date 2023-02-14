package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFileAdminProperty
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasyFileAdminProperties.PREFIX)
public class EasyFileAdminProperties {

    public static final String PREFIX = "easyfile.admin";

    /**
     * admin account
     */
    private Admin admin = new Admin();

    public static class Admin {

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

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}
