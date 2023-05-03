package com.openquartz.easyfile.starter.local.spring.boot.autoconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFileLocalProperties
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasyFileLocalProperties.PREFIX)
public class EasyFileLocalProperties {

    public static final String PREFIX = "easyfile.local";

    /**
     * 数据源连接配置
     */
    private EasyFileDataSourceProperties datasource = new EasyFileDataSourceProperties();

    /**
     * 表配置
     */
    private EasyFileTableProperties table = new EasyFileTableProperties();

    public EasyFileDataSourceProperties getDatasource() {
        return datasource;
    }

    public void setDatasource(
        EasyFileDataSourceProperties datasource) {
        this.datasource = datasource;
    }

    public EasyFileTableProperties getTable() {
        return table;
    }

    public void setTable(
        EasyFileTableProperties table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "EasyFileLocalProperties{" +
            "datasource=" + datasource +
            ", table=" + table +
            '}';
    }

    public static class EasyFileTableProperties {

        private String prefix = "ef";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return "EasyFileTableProperties{" +
                "prefix='" + prefix + '\'' +
                '}';
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

        @Override
        public String toString() {
            return "EasyFileDataSourceProperties{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
        }
    }


}
