package com.openquartz.easyfile.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

/**
 * RemoteExampleApplication
 *
 * @author svnee
 **/
@SpringBootApplication
@ComponentScans(value = {@ComponentScan("com.openquartz.easyfile")})
@MapperScan("com.openquartz.easyfile.example.mapper")
public class RemoteExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteExampleApplication.class);
    }

}
