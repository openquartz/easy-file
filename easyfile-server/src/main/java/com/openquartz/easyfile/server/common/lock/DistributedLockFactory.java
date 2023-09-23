package com.openquartz.easyfile.server.common.lock;

import com.openquartz.easyfile.common.bean.Pair;
import java.util.concurrent.locks.Lock;

/**
 * Distributed EventLock
 *
 * @author svnee
 */
public interface DistributedLockFactory {

    /**
     * Get Lock
     *
     * @param lockKey lockKey
     * @return lock must not be null
     */
    Lock getLock(Pair<String, LockBizType> lockKey);

}
