package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 默认异步处理线程池配置
 * @author svnee
 **/
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = DefaultAsyncHandlerThreadPoolProperties.PREFIX)
public class DefaultAsyncHandlerThreadPoolProperties {

    public static final String PREFIX = "easyfile.default.async.download.handler.thread-pool";

    /**
     * 核心线程数
     */
    private Integer corePoolSize = 10;

    /**
     * 最大线程池数
     */
    private Integer maximumPoolSize = 20;

    /**
     * 单位：秒
     */
    private Long keepAliveTime = 30L;

    /**
     * 阻塞队列最大长度
     */
    private Integer maxBlockingQueueSize = 2048;

    @Override
    public String toString() {
        return "DefaultAsyncHandlerThreadPoolProperties{" +
            "corePoolSize=" + corePoolSize +
            ", maximumPoolSize=" + maximumPoolSize +
            ", keepAliveTime=" + keepAliveTime +
            ", maxBlockingQueueSize=" + maxBlockingQueueSize +
            '}';
    }
}