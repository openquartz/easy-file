package com.openquartz.easyfile.server.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * ILock
 *
 * @author svnee
 */
public interface ILock {

    /**
     * 锁定
     *
     * @param waitTime 获取锁最长等待时间
     * @param leaseTime 锁定时间
     * @param unit 时间单位
     * @return 获取锁结果
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 解锁
     */
    void unlock();

    /**
     * 仅在调用时空闲时才获取锁。
     * 如果可用，则获取锁并立即返回值为true 。 如果锁不可用，则此方法将立即返回false值。
     * 这种方法的典型用法是：
     *
     * Lock lock = ...;
     * if (lock.tryLock()) {
     * try {
     * // manipulate protected state
     * } finally {
     * lock.unlock();
     * }
     * } else {
     * // perform alternative actions
     * }
     * 此用法可确保在获得锁时解锁，如果未获得锁，则不会尝试解锁。
     *
     * @return 如果获得锁则为true ，否则为false
     */
    boolean tryLock();

    /**
     * 是否锁定
     *
     * @return true/false
     */
    boolean isLocked();
}
