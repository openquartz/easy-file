package org.svnee.easyfile.starter.spring.boot.autoconfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.expand.ExportLimitingExecutor;
import org.svnee.easyfile.storage.expand.NoneExportLimitingExecutor;
import org.svnee.easyfile.storage.impl.LocalDownloadStorageServiceImpl;
import org.svnee.easyfile.storage.impl.LocalLimitingServiceImpl;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.mapper.impl.AsyncDownloadRecordMapperImpl;
import org.svnee.easyfile.storage.mapper.impl.AsyncDownloadTaskMapperImpl;

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

    private static final String DEFAULT_DATASOURCE_TYPE = "org.apache.tomcat.jdbc.pool.DataSource";

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    public DownloadStorageService localDownloadStorageService(AsyncDownloadRecordMapper asyncDownloadRecordMapper,
        AsyncDownloadTaskMapper asyncDownloadTaskMapper) {
        return new LocalDownloadStorageServiceImpl(asyncDownloadTaskMapper, asyncDownloadRecordMapper);
    }

    @Bean
    @ConditionalOnMissingBean(LimitingService.class)
    @ConditionalOnClass(LocalLimitingServiceImpl.class)
    public LimitingService localLimitingService(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        List<ExportLimitingExecutor> executorList) {
        return new LocalLimitingServiceImpl(asyncDownloadTaskMapper, executorList);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource easyFileLocalStorageDataSource(EasyFileLocalProperties easyFileLocalProperties,
        Environment environment) {

        Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources
            .get(environment);
        Binder binder = new Binder(sources);
        Properties properties = binder.bind(EasyFileLocalProperties.PREFIX, Properties.class).get();

        DataSource dataSource = buildDataSource(easyFileLocalProperties);
        buildDataSourceProperties(dataSource, properties);
        return dataSource;
    }

    private void buildDataSourceProperties(DataSource dataSource, Map<Object, Object> dsMap) {
        try {
            BeanUtils.copyProperties(dataSource, dsMap);
        } catch (Exception e) {
            log.error("error copy properties", e);
        }
    }

    private DataSource buildDataSource(EasyFileLocalProperties easyFileLocalProperties) {
        String dataSourceType = easyFileLocalProperties.getType();
        try {
            String className = StringUtils.isNotBlank(dataSourceType) ? dataSourceType : DEFAULT_DATASOURCE_TYPE;
            Class<? extends DataSource> type = (Class<? extends DataSource>) Class.forName(className);
            String driverClassName = easyFileLocalProperties.getDriverClassName();
            String url = easyFileLocalProperties.getUrl();
            String username = easyFileLocalProperties.getUsername();
            String password = easyFileLocalProperties.getPassword();

            return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .type(type)
                .build();

        } catch (ClassNotFoundException e) {
            log.error("buildDataSource error", e);
            throw new IllegalStateException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(AsyncDownloadTaskMapper.class)
    @ConditionalOnClass(AsyncDownloadTaskMapper.class)
    public AsyncDownloadTaskMapper asyncDownloadTaskMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new AsyncDownloadTaskMapperImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate localStorageJdbcTemplate(@Qualifier("easyFileLocalStorageDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(AsyncDownloadRecordMapper.class)
    @ConditionalOnClass(AsyncDownloadRecordMapper.class)
    public AsyncDownloadRecordMapper asyncDownloadRecordMapper(JdbcTemplate jdbcTemplate) {
        return new AsyncDownloadRecordMapperImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnClass(ExportLimitingExecutor.class)
    public ExportLimitingExecutor noneExportLimitingExecutor() {
        return new NoneExportLimitingExecutor();
    }

}
