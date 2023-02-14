package org.svnee.easyfile.admin.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.svnee.easyfile.admin.filter.AuthInterceptor;

/**
 * web mvc 配置
 *
 * @author svnee
 */
@Configuration
public class EasyFileAdminWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }

    /**
     * 释放静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/easyfile-ui/static/**")
            .addResourceLocations("classpath:/easyfile-admin-ui/static/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
