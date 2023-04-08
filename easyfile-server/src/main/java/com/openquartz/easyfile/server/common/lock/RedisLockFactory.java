package com.openquartz.easyfile.server.common.lock;

import com.openquartz.easyfile.server.config.BizConfig;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * RedisLock Factory
 *
 * @author svnee
 **/
@Component
@RequiredArgsConstructor
public class RedisLockFactory implements LockFactory {

    private final RedissonClient redissonClient;
    private final BizConfig bizConfig;

    @Override
    public ILock getLock(LockKeyFormatter formatter, Object... placeholder) {
        String key = formatter.generateLockKey(bizConfig.getAppId(), placeholder);
        return new RedisLock(redissonClient.getLock(key));
    }

}
