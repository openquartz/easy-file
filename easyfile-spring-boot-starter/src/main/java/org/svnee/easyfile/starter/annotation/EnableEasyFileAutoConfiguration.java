package org.svnee.easyfile.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.svnee.easyfile.common.constants.Constants;

/**
 * 开启EasyFile auto configuration
 *
 * @author svnee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@ComponentScans(value = {@ComponentScan(Constants.BASE_PACKAGE_PATH)})
public @interface EnableEasyFileAutoConfiguration {


}
