package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.common.util.JSONUtil;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Server-side Redis idempotent import manager.
 * Key scheme must match core {@code RedisIdempotentImportManager}: {@code import:idempotent:{key}}.
 */
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
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void markAsExecuted(String importKey, ImportResult result) {
        String key = IDEMPOTENT_KEY_PREFIX + importKey;
        redisTemplate.opsForValue().set(key, JSONUtil.toJson(result), IDEMPOTENT_TTL);
    }

    @Override
    public ImportResult getExecutedResult(String importKey) {
        String key = IDEMPOTENT_KEY_PREFIX + importKey;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return JSONUtil.parseObject(value, ImportResult.class);
    }

    @Override
    public void clear(String importKey) {
        if (importKey == null || importKey.isEmpty()) {
            return;
        }
        redisTemplate.delete(IDEMPOTENT_KEY_PREFIX + importKey);
    }
}
