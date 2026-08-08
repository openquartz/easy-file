package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

/**
 * 异步导入触发类型
 *
 * @author svnee
 */
public enum ImportAsyncTriggerType {

    /**
     * 线程池触发
     */
    THREAD_POOL,

    /**
     * DB 定时调度触发
     */
    DB_SCHEDULE,

}
