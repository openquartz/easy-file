package com.openquartz.easyfile.example.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages"); // 设置基础名，指向资源文件的位置
        messageSource.setDefaultEncoding("UTF-8"); // 设置默认编码
        return messageSource;
    }
}