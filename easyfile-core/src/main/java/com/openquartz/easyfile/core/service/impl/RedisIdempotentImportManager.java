package com.openquartz.easyfile.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于Redis的幂等性导入管理器实现
 *
 * @author svnee
 */
@Service
public class RedisIdempotentImportManager implements IdempotentImportManager {

    private static final String IDEMPOTENT_KEY_PREFIX = "import:idempotent:";
    private static final Duration IDEMPOTENT_TTL = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotentImportManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isExecuted(String importKey) {
        String key = IDEMPOTENT_KEY_PREFIX + importKey;
        return redisTemplate.hasKey(key);
    }

    @Override
    public void markAsExecuted(String importKey, ImportResult result) {
        String key = IDEMPOTENT_KEY_PREFIX + importKey;
        redisTemplate.opsForValue().set(key, JSON.toJSONString(result), IDEMPOTENT_TTL);
    }

    @Override
    public ImportResult getExecutedResult(String importKey) {
        String key = IDEMPOTENT_KEY_PREFIX + importKey;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return JSON.parseObject(value, ImportResult.class);
    }

    @Override
    public void clear(String importKey) {
        if (importKey == null || importKey.isEmpty()) {
            return;
        }
        redisTemplate.delete(IDEMPOTENT_KEY_PREFIX + importKey);
    }
}
