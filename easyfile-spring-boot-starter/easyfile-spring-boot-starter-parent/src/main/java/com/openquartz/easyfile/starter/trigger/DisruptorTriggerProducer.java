package com.openquartz.easyfile.starter.trigger;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import org.springframework.beans.factory.DisposableBean;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.core.executor.trigger.TriggerMessage;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;

/**
 * Disruptor-发送
 *
 * @author svnee
 **/
public class DisruptorTriggerProducer implements MQTriggerProducer, DisposableBean {

    private final Disruptor<TriggerMessage> disruptor;
    private final RingBuffer<TriggerMessage> ringBuffer;

    private static final EventTranslatorVararg<TriggerMessage> TRANSLATOR =
        (message, seq, objs) -> {
            message.setRegisterId((Long) objs[0]);
            message.setTriggerTimestamp((Long) objs[1]);
        };

    public DisruptorTriggerProducer(DisruptorAsyncHandlerProperties mqAsyncHandlerProperties) {

        ThreadFactory factory = new ThreadFactoryBuilder()
            .setNameFormat(mqAsyncHandlerProperties.getThreadPoolThreadPrefix() + "-thread-%d")
            .build();

        this.disruptor = new Disruptor<>(new DownloadTriggerMessageEventFactory(),
            mqAsyncHandlerProperties.getRingBufferSize(), factory);
        this.ringBuffer = disruptor.getRingBuffer();
    }

    public void register(EventHandler<TriggerMessage> eventHandler) {
        this.disruptor.handleEventsWith(eventHandler);
    }

    public void start() {
        this.disruptor.start();
    }

    private void send(Long registerId, Long triggerTimestamp) {
        this.ringBuffer.publishEvent(TRANSLATOR, registerId, triggerTimestamp);
    }


    @Override
    public boolean send(TriggerMessage triggerMessage) {
        send(triggerMessage.getRegisterId(), triggerMessage.getTriggerTimestamp());
        return true;
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(disruptor)) {
            disruptor.shutdown();
        }
    }

    public void registerAndStart(DisruptorTriggerConsumer consumer) {
        register(consumer);
        start();
    }
}
