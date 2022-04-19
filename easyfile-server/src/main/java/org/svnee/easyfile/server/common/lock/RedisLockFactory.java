package org.svnee.easyfile.server.common.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.server.config.BizConfig;

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
