package org.svnee.easyfile.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件服务Application
 *
 * @author svnee
 **/
@MapperScan("org.svnee.easyfile.server.mapper")
@SpringBootApplication
public class EasyFileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyFileServerApplication.class, args);
    }

}
