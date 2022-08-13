package org.svnee.easyfile.starter.spring.boot.autoconfig.properties;

/**
 * MQ AsyncHandlerProperties
 *
 * @author svnee
 **/
public interface MqAsyncHandlerProperties extends DatabaseAsyncHandlerProperties {

    /**
     * 是否启用
     *
     * @return 是否启用
     */
    default boolean isEnable() {
        return false;
    }

    /**
     * 最大等待超时时间
     *
     * @return 超时时间
     */
    default Integer getMaxWaitingTimeout() {
        return 1600;
    }

    /**
     * 偏移量
     *
     * @return offset
     */
    default Integer getOffset() {
        return 500;
    }
}
