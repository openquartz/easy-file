package com.openquartz.easyfile.server.config;

import com.openquartz.easyfile.server.config.property.EasyFileCommonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CommonConfig
 *
 * @author svnee
 * @since 1.2.3
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileCommonProperty.class)
public class EasyFileCommonConfig {

}
