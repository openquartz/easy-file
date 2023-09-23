package com.openquartz.easyfile.server.common.lock;

import com.openquartz.easyfile.common.bean.Pair;
import java.util.concurrent.locks.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

/**
 * RedisLock
 *
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockFactory implements DistributedLockFactory {

    private final RedissonClient redissonClient;
    private final String appId;

    @Override
    public Lock getLock(Pair<String, LockBizType> lockKey) {
        String distributedLockKey = appId + ":" + lockKey.getValue().getCode() + ":" + lockKey.getKey();
        return redissonClient.getLock(distributedLockKey);
    }
}
