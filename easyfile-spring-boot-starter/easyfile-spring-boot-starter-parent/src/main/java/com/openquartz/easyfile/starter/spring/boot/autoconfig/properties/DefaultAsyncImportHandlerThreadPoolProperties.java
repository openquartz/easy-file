package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 默认异步导入处理线程池配置
 *
 * @author svnee
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = DefaultAsyncImportHandlerThreadPoolProperties.PREFIX)
public class DefaultAsyncImportHandlerThreadPoolProperties {

    public static final String PREFIX = "easyfile.default.async.import.handler.thread-pool";

    /**
     * 线程名前缀
     */
    private String threadPrefix = "DefaultAsyncImportHandler";

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
        return "DefaultAsyncImportHandlerThreadPoolProperties{" +
            "corePoolSize=" + corePoolSize +
            ", maximumPoolSize=" + maximumPoolSize +
            ", keepAliveTime=" + keepAliveTime +
            ", maxBlockingQueueSize=" + maxBlockingQueueSize +
            '}';
    }
}
