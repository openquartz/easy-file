package org.svnee.easyfile.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

/**
 * 文件服务Application
 *
 * @author svnee
 **/
@SpringBootApplication
@MapperScan("org.svnee.easyfile.server.mapper")
@ComponentScans(value = {@ComponentScan("org.svnee")})
public class EasyFileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyFileServerApplication.class, args);
    }

}
