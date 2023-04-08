package com.openquartz.easyfile.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.openquartz.easyfile.starter.annotation.EnableEasyFileAutoConfiguration;

/**
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyFileAutoConfiguration
@MapperScan("com.openquartz.easyfile.example.mapper")
public class LocalExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalExampleApplication.class);
    }
}
