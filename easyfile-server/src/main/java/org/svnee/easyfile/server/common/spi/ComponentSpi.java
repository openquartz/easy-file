package org.svnee.easyfile.server.common.spi;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Spi
 *
 * @author svnee
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Component
public @interface ComponentSpi {

    /**
     * 类型
     */
    Class<? extends ServiceProvider> type();

    /**
     * 服务提供名
     */
    String providerName();

}