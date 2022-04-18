package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.impl.LocalDownloadStorageServiceImpl;
import org.svnee.easyfile.storage.impl.LocalLimitingServiceImpl;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;

/**
 * EasyFileLocalStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileLocalProperties.class)
@ConditionalOnClass({LocalLimitingServiceImpl.class, LocalDownloadStorageServiceImpl.class})
public class EasyFileLocalStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    public DownloadStorageService localDownloadStorageService(AsyncDownloadRecordMapper asyncDownloadRecordMapper,
        AsyncDownloadTaskMapper asyncDownloadTaskMapper) {
        return new LocalDownloadStorageServiceImpl(asyncDownloadTaskMapper, asyncDownloadRecordMapper);
    }

    @Bean
    @ConditionalOnMissingBean(LimitingService.class)
    @ConditionalOnClass(LocalLimitingServiceImpl.class)
    public LimitingService localLimitingService() {
        return new LocalLimitingServiceImpl();
    }


}
