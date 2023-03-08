package org.svnee.easyfile.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.server.config.property.EasyFileCommonProperty;

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
