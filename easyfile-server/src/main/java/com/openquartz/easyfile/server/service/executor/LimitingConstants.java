package com.openquartz.easyfile.server.service.executor;

/**
 * 常量
 *
 * @author svnee
 */
public class LimitingConstants {

    private LimitingConstants() {
    }

    public static final String LIMITING_PREFIX = "Limiting_";

    /**
     * 无
     */
    public static final String NONE = "NONE";

    /**
     * 按照服务全局进行限流
     */
    public static final String GLOBAL_APP = "GLOBAL_APP";

}
