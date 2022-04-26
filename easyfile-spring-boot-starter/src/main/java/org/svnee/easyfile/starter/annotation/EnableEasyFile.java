package org.svnee.easyfile.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileCreatorAutoConfiguration;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileLocalStorageAutoConfiguration;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileRemoteStorageAutoConfiguration;

/**
 * 启动类
 *
 * @author svnee
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EasyFileCreatorAutoConfiguration.class, EasyFileLocalStorageAutoConfiguration.class,
    EasyFileRemoteStorageAutoConfiguration.class})
public @interface EnableEasyFile {

}
