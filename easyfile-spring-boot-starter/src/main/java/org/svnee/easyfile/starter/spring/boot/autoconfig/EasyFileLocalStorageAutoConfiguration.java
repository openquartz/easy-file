package org.svnee.easyfile.starter.spring.boot.autoconfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileLocalProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.expand.ExportLimitingExecutor;
import org.svnee.easyfile.storage.expand.NoneExportLimitingExecutor;
import org.svnee.easyfile.storage.impl.LocalDownloadStorageServiceImpl;
import org.svnee.easyfile.storage.impl.LocalDownloadTriggerServiceImpl;
import org.svnee.easyfile.storage.impl.LocalLimitingServiceImpl;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTriggerMapper;
import org.svnee.easyfile.storage.mapper.impl.AsyncDownloadRecordMapperImpl;
import org.svnee.easyfile.storage.mapper.impl.AsyncDownloadTaskMapperImpl;
import org.svnee.easyfile.storage.mapper.impl.AsyncDownloadTriggerMapperImpl;
import org.svnee.easyfile.storage.prop.EasyFileTableGeneratorSupplier;

/**
 * EasyFileLocalStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileLocalProperties.class})
@ConditionalOnClass({LocalLimitingServiceImpl.class, LocalDownloadStorageServiceImpl.class})
@AutoConfigureBefore(EasyFileCreatorAutoConfiguration.class)
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileLocalStorageAutoConfiguration implements InitializingBean {

    private static final String DEFAULT_DATASOURCE_TYPE = "org.apache.tomcat.jdbc.pool.DataSource";

    @Resource
    private EasyFileLocalProperties easyFileLocalProperties;

    private DataSource newLocalStorageDataSource(EasyFileLocalProperties easyFileLocalProperties,
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

    @Bean
    @ConditionalOnMissingBean(DownloadTriggerService.class)
    public DownloadTriggerService localDownloadTriggerService(AsyncDownloadTriggerMapper asyncDownloadTriggerMapper) {
        return new LocalDownloadTriggerServiceImpl(asyncDownloadTriggerMapper);
    }

    private DataSource buildDataSource(EasyFileLocalProperties easyFileLocalProperties) {
        String dataSourceType = easyFileLocalProperties.getDatasource().getType();
        try {
            String className = StringUtils.isNotBlank(dataSourceType) ? dataSourceType : DEFAULT_DATASOURCE_TYPE;
            Class<? extends DataSource> type = (Class<? extends DataSource>) Class.forName(className);
            String driverClassName = easyFileLocalProperties.getDatasource().getDriverClassName();
            String url = easyFileLocalProperties.getDatasource().getUrl();
            String username = easyFileLocalProperties.getDatasource().getUsername();
            String password = easyFileLocalProperties.getDatasource().getPassword();

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
    @ConditionalOnMissingBean(type = "localStorageJdbcTemplate", value = JdbcTemplate.class)
    public JdbcTemplate localStorageJdbcTemplate(EasyFileLocalProperties easyFileLocalProperties,
        Environment environment) {
        return new JdbcTemplate(newLocalStorageDataSource(easyFileLocalProperties, environment));
    }

    @Bean
    @ConditionalOnMissingBean(AsyncDownloadTaskMapper.class)
    @ConditionalOnClass(AsyncDownloadTaskMapper.class)
    public AsyncDownloadTaskMapper asyncDownloadTaskMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncDownloadTaskMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AsyncDownloadRecordMapper.class)
    @ConditionalOnClass(AsyncDownloadRecordMapper.class)
    public AsyncDownloadRecordMapper asyncDownloadRecordMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncDownloadRecordMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AsyncDownloadTriggerMapper.class)
    @ConditionalOnClass(AsyncDownloadTriggerMapper.class)
    public AsyncDownloadTriggerMapper asyncDownloadTriggerMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncDownloadTriggerMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnClass(ExportLimitingExecutor.class)
    public ExportLimitingExecutor noneExportLimitingExecutor() {
        return new NoneExportLimitingExecutor();
    }

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    @ConditionalOnClass(LocalDownloadStorageServiceImpl.class)
    public DownloadStorageService localDownloadStorageServiceImpl(AsyncDownloadRecordMapper asyncDownloadRecordMapper,
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

    @Override
    public void afterPropertiesSet() {
        EasyFileTableGeneratorSupplier.setPrefix(easyFileLocalProperties.getTable().getPrefix());
    }
}
