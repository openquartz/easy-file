package com.openquartz.easyfile.server.config.persistence;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mybatis Extra Config
 *
 * @author svnee
 **/
@Configuration
public class MybatisExtraConfig {

    /**
     * 分頁拦截器
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

}
