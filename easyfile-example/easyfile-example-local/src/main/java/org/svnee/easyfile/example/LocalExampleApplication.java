package org.svnee.easyfile.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.svnee.easyfile.starter.annotation.EnableEasyFileAutoConfiguration;

/**
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyFileAutoConfiguration
@MapperScan("org.svnee.easyfile.example.mapper")
public class LocalExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalExampleApplication.class);
    }
}
