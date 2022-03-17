package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * @author svnee
 * @desc spring-配置核心类
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncHandlerThreadPoolProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileCreatorAutoConfiguration {


}
