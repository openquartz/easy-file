package com.openquartz.easyfile.example.config;

import com.openquartz.easyfile.example.upload.ActualUploadServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
@Configuration
public class EasyFileConfiguration {

    @Bean
    public UploadService uploadService() {
        return new ActualUploadServiceImpl();
    }

}
