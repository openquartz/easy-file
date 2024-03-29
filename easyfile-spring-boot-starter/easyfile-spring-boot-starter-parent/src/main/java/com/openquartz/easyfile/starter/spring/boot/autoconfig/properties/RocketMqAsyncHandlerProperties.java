package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 配置
 *
 * @author svnee
 **/
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = RocketMqAsyncHandlerProperties.PREFIX)
public class RocketMqAsyncHandlerProperties implements MqAsyncHandlerProperties {

    public static final String PREFIX = "easyfile.rocketmq.async.download.handler";

    /**
     * Topic
     */
    private String topic = "easyfile_mq_trigger";

    /**
     * host
     */
    private String host = "127.0.0.1:9876";

    /**
     * 生产组
     */
    private String produceGroup = "p_async_handler_group";

    /**
     * 发送超时
     * 单位：秒
     */
    private Integer produceTimeout = 1000;

    /**
     * 重试次数
     */
    private int produceTryTimes = 5;

    /**
     * 故障轉移
     */
    private boolean produceLatencyFaultEnable = true;

    /**
     * 消費組
     */
    private String consumerGroup = "c_async_handler_group";

    /**
     * 最大消费者数
     */
    private Integer consumerMaxThread = 3;

    /**
     * 最小消费者数
     */
    private Integer consumerMinThread = 1;

    /**
     *
     */
    private Integer consumeConcurrentlyMaxSpan = 10;

    /**
     * 最大执行超时时间 单位秒
     * 超出此时间认定为执行失败。重新执行
     */
    private Integer maxExecuteTimeout = 1600;

    /**
     * 等待超时
     * 单位：秒
     * 超时将重新触发执行
     */
    private Integer maxWaitingTimeout = 1600;

    /**
     * 调度周期 单位：秒
     */
    private Integer schedulePeriod = 10;

    /**
     * 回溯时间
     * 单位：小时
     */
    private Integer lookBackHours = 2;

    /**
     * 最大重试次数
     */
    private Integer maxTriggerCount = 5;

    /**
     * 最大归档小时
     */
    private Integer maxArchiveHours = 24;

    /**
     * offset
     */
    private Integer offset = 500;

}
