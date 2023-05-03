package com.openquartz.easyfile.starter.remote.spring.boot.autoconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调用远程服务配置
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasyFileRemoteProperties.PREFIX)
public class EasyFileRemoteProperties {

    public static final String PREFIX = "easyfile.remote";

    /**
     * Username.
     * username
     */
    private String username;

    /**
     * Password.
     * password
     */
    private String password;

    /**
     * Server addr
     * server-addr
     */
    private String serverAddr;

    /**
     * Namespace
     * namespace
     */
    private String namespace;

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

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "EasyFileRemoteProperties{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", serverAddr='" + serverAddr + '\'' +
            ", namespace='" + namespace + '\'' +
            '}';
    }
}
