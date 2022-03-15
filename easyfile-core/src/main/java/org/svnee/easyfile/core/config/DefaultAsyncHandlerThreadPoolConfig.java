package org.svnee.easyfile.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author svnee
 * @desc
 **/
@Component
@ConfigurationProperties(prefix = "easyfile.default.async.download.handler.thread-pool")
public class DefaultAsyncHandlerThreadPoolConfig {

    private Integer corePoolSize = 10;

    private Integer maximumPoolSize = 20;

    /**
     * 单位：秒
     */
    private Long keepAliveTime = 30L;

    /**
     * 阻塞队列最大长度
     */
    private Integer maxBlockingQueueSize = 2048;

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public Integer getMaxBlockingQueueSize() {
        return maxBlockingQueueSize;
    }

    public void setMaxBlockingQueueSize(Integer maxBlockingQueueSize) {
        this.maxBlockingQueueSize = maxBlockingQueueSize;
    }

    @Override
    public String toString() {
        return "AdsDefaultAsyncHandlerThreadPoolConfig{" +
            "corePoolSize=" + corePoolSize +
            ", maximumPoolSize=" + maximumPoolSize +
            ", keepAliveTime=" + keepAliveTime +
            ", maxBlockingQueueSize=" + maxBlockingQueueSize +
            '}';
    }
}