package org.svnee.easyfile.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.svnee.easyfile.starter.annotation.EnableEasyFile;

/**
 * @author svnee
 **/
@SpringBootApplication
@ComponentScans(value = {@ComponentScan("org.svnee.easyfile")})
@MapperScan("org.svnee.easyfile.example.mapper")
@EnableEasyFile
public class LocalExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalExampleApplication.class);
    }
}
