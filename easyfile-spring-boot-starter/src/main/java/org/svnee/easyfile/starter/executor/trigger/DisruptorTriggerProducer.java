package org.svnee.easyfile.starter.executor.trigger;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import org.springframework.beans.factory.DisposableBean;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;


/**
 * Disruptor-发送
 *
 * @author svnee
 **/
public class DisruptorTriggerProducer implements MQTriggerProducer, DisposableBean {


    private final Disruptor<DownloadTriggerMessage> disruptor;
    private final RingBuffer<DownloadTriggerMessage> ringBuffer;

    private static final EventTranslatorVararg<DownloadTriggerMessage> TRANSLATOR =
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

    public void register(EventHandler<DownloadTriggerMessage> eventHandler) {
        this.disruptor.handleEventsWith(eventHandler);
    }

    public void start() {
        this.disruptor.start();
    }

    private void send(Long registerId, Long triggerTimestamp) {
        this.ringBuffer.publishEvent(TRANSLATOR, registerId, triggerTimestamp);
    }


    @Override
    public boolean send(DownloadTriggerMessage triggerMessage) {
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
