package org.svnee.easyfile.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web mvc 配置
 *
 * @author svnee
 */
@Configuration
public class EasyFileAdminWebMvcConfig implements WebMvcConfigurer {

    /**
     * 释放静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/easyfile-ui/static/**").addResourceLocations("classpath:/easyfile-admin-ui/static/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
