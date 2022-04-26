package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 默认异步处理线程池配置
 *
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
     * core-pool-size
     */
    private Integer corePoolSize = 10;

    /**
     * 最大线程池数
     * maximum-pool-size
     */
    private Integer maximumPoolSize = 20;

    /**
     * 单位：秒
     * keep-alive-time
     */
    private Long keepAliveTime = 30L;

    /**
     * 阻塞队列最大长度
     * max-blocking-queue-size
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