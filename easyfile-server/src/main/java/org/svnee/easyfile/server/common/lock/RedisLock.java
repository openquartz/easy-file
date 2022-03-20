package org.svnee.easyfile.server.common.lock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

/**
 * RedisLock
 *
 * @author zhaoli@100.me
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLock implements ILock {

    private final RLock redLock;

    /**
     * 锁定
     *
     * @param waitTime 获取锁最长等待时间
     * @param leaseTime 锁定时间
     * @param unit 时间单位
     * @return 获取锁结果
     */
    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redLock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException ex) {
            log.error("get redis lock error,key:{},ex:", redLock.getName(), ex);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 解锁
     */
    @Override
    public void unlock() {
        try {
            redLock.unlock();
        } catch (Exception ex) {
            log.warn("[unlock fail]:", ex);
        }
    }

    @Override
    public boolean tryLock() {
        return redLock.tryLock();
    }

    @Override
    public boolean isLocked() {
        return redLock.isLocked();
    }
}
