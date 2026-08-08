package com.openquartz.easyfile.server.config;

import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.server.service.impl.RedisIdempotentImportManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Wires {@link IdempotentImportManager} for admin/remote retry clear of Redis idempotent marks.
 * Server always ships spring-boot-starter-data-redis (same as locks); bean is required for retry parity.
 */
@Configuration
public class IdempotentImportConfiguration {

    @Bean
    public IdempotentImportManager idempotentImportManager(StringRedisTemplate stringRedisTemplate) {
        return new RedisIdempotentImportManager(stringRedisTemplate);
    }
}
