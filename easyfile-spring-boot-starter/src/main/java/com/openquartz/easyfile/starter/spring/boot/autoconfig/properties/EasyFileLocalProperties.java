package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFileLocalProperties
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileLocalProperties.PREFIX)
public class EasyFileLocalProperties {

    public static final String PREFIX = "easyfile.local";

    /**
     * 数据源连接配置
     */
    private final EasyFileDataSourceProperties datasource = new EasyFileDataSourceProperties();

    /**
     * 表配置
     */
    private final EasyFileTableProperties table = new EasyFileTableProperties();

    public static class EasyFileTableProperties {

        private String prefix = "ef";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public static class EasyFileDataSourceProperties {

        /**
         * type
         */
        private String type;

        /**
         * jdbcUrl
         */
        private String url;

        /**
         * driver-class-name
         */
        private String driverClassName;

        /**
         * username
         */
        private String username;

        /**
         * password
         */
        private String password;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

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


}
