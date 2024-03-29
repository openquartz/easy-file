package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileUiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.service.DefaultServiceAppIdProvider;
import com.openquartz.easyfile.admin.service.LoginService;
import com.openquartz.easyfile.admin.service.ServerAppIdProvider;

/**
 * Admin Configuration
 *
 * @author svnee
 * @since 1.2.0
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileUiProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(LoginService.class)
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileUiAutoConfiguration {

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

    @Bean
    public ServerAppIdProvider defaultServerAppIdProvider(EasyFileDownloadProperties easyFileDownloadProperties) {
        return new DefaultServiceAppIdProvider(easyFileDownloadProperties);
    }

}
