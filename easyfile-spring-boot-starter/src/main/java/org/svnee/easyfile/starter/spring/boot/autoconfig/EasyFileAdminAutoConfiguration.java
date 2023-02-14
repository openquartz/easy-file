package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(LoginService.class)
public class EasyFileAdminAutoConfiguration {

    @Bean
    public LoginService loginService(AdminProperty adminProperty){
        return new LoginService(adminProperty);
    }

    @Bean
    public AdminProperty adminProperty(EasyFileAdminProperties properties){
        AdminProperty adminProperty = new AdminProperty();
        adminProperty.setAdminUsername(properties.getAdmin().getUsername());
        adminProperty.setAdminPassword(properties.getAdmin().getPassword());
        return adminProperty;
    }

}
