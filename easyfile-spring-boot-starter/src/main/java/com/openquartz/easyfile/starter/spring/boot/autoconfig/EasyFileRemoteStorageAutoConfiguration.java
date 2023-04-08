package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileRemoteProperties;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.remote.impl.HttpEasyFileClientImpl;
import com.openquartz.easyfile.storage.remote.impl.RemoteDownloadStorageServiceImpl;
import com.openquartz.easyfile.storage.remote.impl.RemoteLimitingServiceImpl;
import com.openquartz.easyfile.storage.remote.common.HttpAgent;
import com.openquartz.easyfile.storage.remote.common.HttpScheduledHealthCheck;
import com.openquartz.easyfile.storage.remote.common.RemoteBootstrapProperties;
import com.openquartz.easyfile.storage.remote.common.RemoteClient;
import com.openquartz.easyfile.storage.remote.common.ServerHealthCheck;
import com.openquartz.easyfile.storage.remote.common.ServerHttpAgent;

/**
 * EasyFileLocalStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileRemoteProperties.class})
@ConditionalOnClass({RemoteDownloadStorageServiceImpl.class, RemoteLimitingServiceImpl.class})
@AutoConfigureBefore(EasyFileCreatorAutoConfiguration.class)
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileRemoteStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DownloadStorageService.class)
    @ConditionalOnClass(RemoteDownloadStorageServiceImpl.class)
    public DownloadStorageService remoteDownloadStorageService(EasyFileClient easyFileClient) {
        return new RemoteDownloadStorageServiceImpl(easyFileClient);
    }

    @Bean
    @ConditionalOnMissingBean(LimitingService.class)
    @ConditionalOnClass(RemoteLimitingServiceImpl.class)
    public LimitingService remoteLimitingService(EasyFileClient easyFileClient) {
        return new RemoteLimitingServiceImpl(easyFileClient);
    }

    @Bean
    @ConditionalOnClass(RemoteBootstrapProperties.class)
    public RemoteBootstrapProperties remoteBootstrapProperties(EasyFileRemoteProperties easyFileRemoteProperties) {
        RemoteBootstrapProperties remoteBootstrapProperties = new RemoteBootstrapProperties();
        remoteBootstrapProperties.setUsername(easyFileRemoteProperties.getUsername());
        remoteBootstrapProperties.setPassword(easyFileRemoteProperties.getPassword());
        remoteBootstrapProperties.setServerAddr(easyFileRemoteProperties.getServerAddr());
        remoteBootstrapProperties.setNamespace(easyFileRemoteProperties.getNamespace());
        return remoteBootstrapProperties;
    }

    @Bean
    @ConditionalOnClass(RemoteClient.class)
    public RemoteClient remoteClient() {
        return new RemoteClient(new OkHttpClient());
    }

    @Bean
    @ConditionalOnClass(HttpAgent.class)
    @ConditionalOnMissingBean(HttpAgent.class)
    public HttpAgent serverHttpAgent(RemoteBootstrapProperties properties, RemoteClient remoteClient) {
        return new ServerHttpAgent(properties, remoteClient);
    }

    @Bean
    @ConditionalOnClass(OkHttpClient.class)
    @ConditionalOnMissingBean(OkHttpClient.class)
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder build = new OkHttpClient.Builder();
        build.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        supportHttps(build);
        return build.build();
    }

    @SneakyThrows
    private void supportHttps(OkHttpClient.Builder builder) {
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        }};

        final SSLContext sslContext = SSLContext.getInstance(Constants.SSL);
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);
    }

    @Bean
    @ConditionalOnMissingBean(ServerHealthCheck.class)
    @ConditionalOnClass(ServerHealthCheck.class)
    public ServerHealthCheck serverHealthCheck(HttpAgent httpAgent) {
        return new HttpScheduledHealthCheck(httpAgent);
    }

    @Bean
    @ConditionalOnMissingBean(EasyFileClient.class)
    @ConditionalOnClass(EasyFileClient.class)
    public EasyFileClient easyFileClient(HttpAgent httpAgent) {
        return new HttpEasyFileClientImpl(httpAgent);
    }

}
