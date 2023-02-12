package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.admin.property.AdminProperty;
import org.svnee.easyfile.admin.service.LoginService;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileAdminProperties;

/**
 * Admin Configuration
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileAdminProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = EasyFileAdminProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileAdminAutoConfiguration {

    @Bean
    public LoginService loginService(EasyFileAdminProperties properties){
        AdminProperty adminProperty = new AdminProperty();
        adminProperty.setAdminUsername(properties.getAdmin().getUsername());
        adminProperty.setAdminPassword(properties.getAdmin().getPassword());
        return new LoginService(adminProperty);
    }

}