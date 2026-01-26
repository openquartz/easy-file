package com.openquartz.easyfile.starter.local.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.EasyFileCreatorAutoConfiguration;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.local.spring.boot.autoconfig.properties.EasyFileLocalProperties;
import com.openquartz.easyfile.storage.local.expand.NoneExportLimitingExecutor;
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
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.DownloadTriggerService;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.expand.ExportLimitingExecutor;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import com.openquartz.easyfile.storage.importer.ImportTriggerService;
import com.openquartz.easyfile.storage.local.impl.LocalDownloadStorageServiceImpl;
import com.openquartz.easyfile.storage.local.impl.LocalDownloadTriggerServiceImpl;
import com.openquartz.easyfile.storage.local.impl.LocalImportStorageServiceImpl;
import com.openquartz.easyfile.storage.local.impl.LocalImportTriggerServiceImpl;
import com.openquartz.easyfile.storage.local.impl.LocalLimitingServiceImpl;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTriggerMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTaskMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTriggerMapper;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncDownloadRecordMapperImpl;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncDownloadTaskMapperImpl;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncDownloadTriggerMapperImpl;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncImportRecordMapperImpl;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncImportTaskMapperImpl;
import com.openquartz.easyfile.storage.local.mapper.impl.AsyncImportTriggerMapperImpl;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;

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
    @ConditionalOnMissingBean(AsyncImportTaskMapper.class)
    @ConditionalOnClass(AsyncImportTaskMapper.class)
    public AsyncImportTaskMapper asyncImportTaskMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncImportTaskMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AsyncImportRecordMapper.class)
    @ConditionalOnClass(AsyncImportRecordMapper.class)
    public AsyncImportRecordMapper asyncImportRecordMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncImportRecordMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AsyncImportTriggerMapper.class)
    @ConditionalOnClass(AsyncImportTriggerMapper.class)
    public AsyncImportTriggerMapper asyncImportTriggerMapper(
        @Qualifier("localStorageJdbcTemplate") JdbcTemplate localStorageJdbcTemplate) {
        return new AsyncImportTriggerMapperImpl(localStorageJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ImportTriggerService.class)
    public ImportTriggerService localImportTriggerService(AsyncImportTriggerMapper asyncImportTriggerMapper) {
        return new LocalImportTriggerServiceImpl(asyncImportTriggerMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ImportStorageService.class)
    @ConditionalOnClass(LocalImportStorageServiceImpl.class)
    public ImportStorageService localImportStorageServiceImpl(AsyncImportRecordMapper asyncImportRecordMapper,
        AsyncImportTaskMapper asyncImportTaskMapper) {
        return new LocalImportStorageServiceImpl(asyncImportTaskMapper, asyncImportRecordMapper);
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
