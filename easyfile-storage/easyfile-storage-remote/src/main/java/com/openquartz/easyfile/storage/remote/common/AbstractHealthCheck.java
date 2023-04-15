package com.openquartz.easyfile.storage.remote.common;

import static com.openquartz.easyfile.common.constants.Constants.HEALTH_CHECK_INTERVAL;

import com.openquartz.easyfile.storage.remote.exception.RemoteExceptionCode;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import com.openquartz.easyfile.common.event.ApplicationCompleteEvent;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import org.springframework.lang.NonNull;

/**
 * Abstract health check.
 *
 * @author svnee
 */
@Slf4j
public abstract class AbstractHealthCheck implements ServerHealthCheck, InitializingBean,
    ApplicationListener<ApplicationCompleteEvent> {

    /**
     * Health status
     */
    private volatile boolean healthStatus = true;

    /**
     * Client shutdown hook
     */
    private volatile boolean clientShutdownHook = false;

    /**
     * Spring context init complete
     */
    private boolean contextInitComplete = false;

    /**
     * Health main lock
     */
    private final ReentrantLock healthMainLock = new ReentrantLock();

    /**
     * Health condition
     */
    private final Condition healthCondition = healthMainLock.newCondition();

    /**
     * Health check executor
     */
    private final ScheduledThreadPoolExecutor healthCheckExecutor = new ScheduledThreadPoolExecutor(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("ServerHealthCheck-thread-%d")
            .build()
    );

    /**
     * Send health check.
     *
     */
    protected abstract boolean sendHealthCheck();

    /**
     * Health check.
     */
    public void healthCheck() {
        boolean healthCheckStatus = sendHealthCheck();
        if (healthCheckStatus) {
            if (Objects.equals(healthStatus, false)) {
                healthStatus = true;
                log.info("The client reconnects to the server successfully.");
                signalAllBizThread();
            }
        } else {
            healthStatus = false;
        }
    }

    @Override
    @SneakyThrows
    public boolean isHealthStatus() {
        while (contextInitComplete
                && !healthStatus && !clientShutdownHook) {
            healthMainLock.lock();
            try {
                healthCondition.await();
            } finally {
                healthMainLock.unlock();
            }
        }

        if (!healthStatus) {
            throw new EasyFileException(RemoteExceptionCode.SHUTDOWN_ERROR);
        }

        return healthStatus;
    }

    @Override
    public void setHealthStatus(boolean healthStatus) {
        healthMainLock.lock();
        try {
            this.healthStatus = healthStatus;
            log.warn("The server health status setting is unavailable.");
        } finally {
            healthMainLock.unlock();
        }
    }

    /**
     * Signal all biz thread.
     */
    private void signalAllBizThread() {
        healthMainLock.lock();
        try {
            healthCondition.signalAll();
        } finally {
            healthMainLock.unlock();
        }
    }

    @Override
    public void afterPropertiesSet() {
        // 添加钩子函数, Client 端停止时, 如果 Server 端是非健康状态, Client 销毁函数会暂停运行
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            clientShutdownHook = true;
            signalAllBizThread();
        }));

        healthCheckExecutor.scheduleWithFixedDelay(this::healthCheck, 0, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationCompleteEvent event) {
        contextInitComplete = true;
    }

}
