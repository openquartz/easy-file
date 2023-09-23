package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;

/**
 * MQ AsyncHandlerProperties
 *
 * @author svnee
 **/
public interface MqAsyncHandlerProperties extends IDatabaseAsyncHandlerProperty {

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
