package org.svnee.easyfile.common.util;

import org.springframework.aop.support.AopUtils;

/**
 * SpringContextUtil
 *
 * @author xuzhao
 */
public class SpringContextUtil {

    private SpringContextUtil() {
    }

    public static Class<?> getRealClass(Object springBean) {
        return AopUtils.isAopProxy(springBean) ? AopUtils.getTargetClass(springBean) : springBean.getClass();
    }

}
