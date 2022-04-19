package org.svnee.easyfile.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.example.upload.ActualUploadServiceImpl;
import org.svnee.easyfile.storage.file.UploadService;

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
