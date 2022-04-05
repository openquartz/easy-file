package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.starter.aop.FileExportExecutorAnnotationAdvisor;
import org.svnee.easyfile.starter.aop.FileExportInterceptor;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.processor.AutoRegisteredDownloadTaskListener;
import org.svnee.easyfile.starter.processor.FileExportExecutorPostProcessor;
import org.svnee.easyfile.storage.EasyFileClient;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.file.UploadService;
import org.svnee.easyfile.storage.file.local.LocalUploadService;
import org.svnee.easyfile.storage.impl.HttpEasyFileClientImpl;
import org.svnee.easyfile.storage.impl.LocalDownloadStorageServiceImpl;
import org.svnee.easyfile.storage.impl.LocalLimitingServiceImpl;
import org.svnee.easyfile.storage.impl.RemoteDownloadStorageServiceImpl;
import org.svnee.easyfile.storage.impl.RemoteLimitingServiceImpl;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.remote.HttpAgent;
import org.svnee.easyfile.storage.remote.HttpScheduledHealthCheck;
import org.svnee.easyfile.storage.remote.RemoteBootstrapProperties;
import org.svnee.easyfile.storage.remote.RemoteClient;
import org.svnee.easyfile.storage.remote.ServerHealthCheck;
import org.svnee.easyfile.storage.remote.ServerHttpAgent;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncHandlerThreadPoolProperties.class, EasyFileDownloadProperties.class,
    EasyFileRemoteProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileCreatorAutoConfiguration {

    @Bean
    public FileExportExecutorPostProcessor fileExportExecutorPostProcessor() {
        return new FileExportExecutorPostProcessor();
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")
    public AutoRegisteredDownloadTaskListener autoRegisteredDownloadTaskListener(
        EasyFileDownloadProperties easyFileDownloadProperties,
        DownloadStorageService downloadStorageService) {
        return new AutoRegisteredDownloadTaskListener(easyFileDownloadProperties, downloadStorageService);
    }

    @Bean
    public Advisor fileExportExecutorAnnotationAdvisor(EasyFileDownloadProperties easyFileDownloadProperties,
        DownloadStorageService downloadStorageService,
        LimitingService limitingService,
        BaseAsyncFileHandler baseAsyncFileHandler,
        ApplicationContext applicationContext
    ) {
        FileExportInterceptor interceptor = new FileExportInterceptor(easyFileDownloadProperties, limitingService,
            downloadStorageService, baseAsyncFileHandler, applicationContext);
        FileExportExecutorAnnotationAdvisor advisor = new FileExportExecutorAnnotationAdvisor(interceptor);
        advisor.setOrder(easyFileDownloadProperties.getExportAdvisorOrder());
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean(BaseDefaultDownloadRejectExecutionHandler.class)
    public BaseDefaultDownloadRejectExecutionHandler defaultDownloadRejectExecutionHandler() {
        return new DefaultDownloadRejectExecutionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileHandler.class)
    public BaseAsyncFileHandler defaultAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler,
        DefaultAsyncHandlerThreadPoolProperties defaultAsyncHandlerThreadPoolProperties) {
        return new DefaultAsyncFileHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            baseDefaultDownloadRejectExecutionHandler,
            defaultAsyncHandlerThreadPoolProperties);
    }

    @Bean
    @ConditionalOnMissingBean(UploadService.class)
    public UploadService localUploadService() {
        return new LocalUploadService();
    }

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    @ConditionalOnClass(LocalDownloadStorageServiceImpl.class)
    public DownloadStorageService localDownloadStorageService(AsyncDownloadRecordMapper asyncDownloadRecordMapper,
        AsyncDownloadTaskMapper asyncDownloadTaskMapper) {
        return new LocalDownloadStorageServiceImpl(asyncDownloadTaskMapper, asyncDownloadRecordMapper);
    }

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    @ConditionalOnClass(RemoteDownloadStorageServiceImpl.class)
    public DownloadStorageService remoteDownloadStorageService(EasyFileClient easyFileClient) {
        return new RemoteDownloadStorageServiceImpl(easyFileClient);
    }

    @Bean
    @ConditionalOnMissingBean(LimitingService.class)
    @ConditionalOnClass(LocalLimitingServiceImpl.class)
    public LimitingService localLimitingService() {
        return new LocalLimitingServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(LimitingService.class)
    @ConditionalOnClass(RemoteLimitingServiceImpl.class)
    public LimitingService remoteLimitingService(EasyFileClient easyFileClient) {
        return new RemoteLimitingServiceImpl(easyFileClient);
    }

    @Bean
    @ConditionalOnClass(RemoteBootstrapProperties.class)
    public RemoteBootstrapProperties remoteBootstrapProperties(EasyFileRemoteProperties easyFileRemoteProperties){
        RemoteBootstrapProperties remoteBootstrapProperties = new RemoteBootstrapProperties();
        remoteBootstrapProperties.setUsername(easyFileRemoteProperties.getUsername());
        remoteBootstrapProperties.setPassword(easyFileRemoteProperties.getPassword());
        remoteBootstrapProperties.setServerAddr(easyFileRemoteProperties.getServerAddr());
        remoteBootstrapProperties.setNamespace(easyFileRemoteProperties.getNamespace());
        remoteBootstrapProperties.setItemId(easyFileRemoteProperties.getItemId());
        remoteBootstrapProperties.setEnable(easyFileRemoteProperties.getEnable());
        remoteBootstrapProperties.setBanner(easyFileRemoteProperties.getBanner());
        remoteBootstrapProperties.setEnableCollect(easyFileRemoteProperties.getEnableCollect());
        remoteBootstrapProperties.setTaskBufferSize(easyFileRemoteProperties.getTaskBufferSize());
        remoteBootstrapProperties.setInitialDelay(easyFileRemoteProperties.getInitialDelay());
        remoteBootstrapProperties.setCollectInterval(easyFileRemoteProperties.getCollectInterval());
        remoteBootstrapProperties.setJsonSerializeType(easyFileRemoteProperties.getJsonSerializeType());
        return remoteBootstrapProperties;
    }

    @Bean
    @ConditionalOnClass(RemoteClient.class)
    public RemoteClient remoteClient(){
        return new RemoteClient(new OkHttpClient());
    }

    @Bean
    @ConditionalOnClass(HttpAgent.class)
    @ConditionalOnMissingBean(HttpAgent.class)
    public HttpAgent serverHttpAgent(RemoteBootstrapProperties properties,RemoteClient remoteClient){
        return new ServerHttpAgent(properties, remoteClient);
    }

    @Bean
    @ConditionalOnMissingBean(ServerHealthCheck.class)
    @ConditionalOnClass(ServerHealthCheck.class)
    public ServerHealthCheck serverHealthCheck(HttpAgent httpAgent){
        return new HttpScheduledHealthCheck(httpAgent);
    }

    @Bean
    @ConditionalOnMissingBean(EasyFileClient.class)
    @ConditionalOnClass(EasyFileClient.class)
    public EasyFileClient easyFileClient(HttpAgent httpAgent){
        return new HttpEasyFileClientImpl(httpAgent);
    }

}
