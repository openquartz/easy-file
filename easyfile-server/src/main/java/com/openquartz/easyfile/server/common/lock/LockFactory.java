package com.openquartz.easyfile.server.common.lock;

/**
 * LockFactory
 *
 * @author svnee
 */
public interface LockFactory {

    /**
     * 获取锁
     *
     * @param formatter 锁格式
     * @param placeholder placeholder
     * @return 分布式锁
     */
    ILock getLock(LockKeyFormatter formatter, Object... placeholder);
}
