package com.openquartz.easyfile.server.config;

import com.openquartz.easyfile.server.config.property.EasyFileUiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.service.LoginService;

/**
 * UiConfig
 *
 * @author svnee
 * @since 1.2.3
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileUiProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(LoginService.class)
public class UiConfig {

    @Bean
    public LoginService loginService(AdminProperty adminProperty) {
        return new LoginService(adminProperty);
    }

    @Bean
    public AdminProperty adminProperty(EasyFileUiProperties properties) {
        AdminProperty adminProperty = new AdminProperty();
        adminProperty.setAdminUsername(properties.getAdmin().getUsername());
        adminProperty.setAdminPassword(properties.getAdmin().getPassword());
        return adminProperty;
    }

}
