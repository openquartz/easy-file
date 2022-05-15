package org.svnee.easyfile.example;

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
@ComponentScans(value = {@ComponentScan("org.svnee.easyfile")})
@MapperScan("org.svnee.easyfile.example.mapper")
public class RemoteExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteExampleApplication.class);
    }

}
