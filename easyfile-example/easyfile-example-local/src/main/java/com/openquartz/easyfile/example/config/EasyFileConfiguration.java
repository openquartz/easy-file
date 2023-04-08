package com.openquartz.easyfile.example.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.openquartz.easyfile.example.upload.ActualUploadServiceImpl;
import com.openquartz.easyfile.example.upload.MinioFileServiceUrlTransformer;
import com.openquartz.easyfile.example.upload.MinioUploadServiceImpl;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
@Configuration
public class EasyFileConfiguration {

    @Bean
    @ConditionalOnMissingBean(UploadService.class)
    public UploadService localUploadService() {
        return new ActualUploadServiceImpl();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(value = "minio.enabled", havingValue = "true")
    public UploadService minioUploadService(MinioProperties minioProperties, MinioClient minioClient) {
        return new MinioUploadServiceImpl(minioProperties, minioClient);
    }

    @Bean
    @ConditionalOnProperty(value = "minio.enabled", havingValue = "true")
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
            .endpoint(minioProperties.getUrl())
            .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
            .build();
    }

    @Bean
    @ConditionalOnProperty(value = "minio.enabled", havingValue = "true")
    public MinioFileServiceUrlTransformer minioFileServiceUrlTransformer(MinioClient minioClient){
        return new MinioFileServiceUrlTransformer(minioClient);
    }
}
