package com.openquartz.easyfile.admin.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录控制
 *
 * @author svnee
 * @since 1.2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

    /**
     * need login
     *
     * @return login
     */
    boolean needLogin() default true;

}