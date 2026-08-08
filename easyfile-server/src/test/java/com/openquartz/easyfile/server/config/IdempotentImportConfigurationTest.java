package com.openquartz.easyfile.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.server.service.impl.RedisIdempotentImportManager;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Smoke: IdempotentImportManager bean is defined when StringRedisTemplate is available.
 */
public class IdempotentImportConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(IdempotentImportConfiguration.class, RedisStubConfig.class);

    @Test
    public void idempotentImportManager_definedWhenStringRedisTemplatePresent() {
        contextRunner.run(context -> {
            assertTrue(context.containsBean("idempotentImportManager"));
            IdempotentImportManager manager = context.getBean(IdempotentImportManager.class);
            assertNotNull(manager);
            assertEquals(RedisIdempotentImportManager.class, manager.getClass());
        });
    }

    @Test
    public void clear_deletesImportIdempotentKey() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        IdempotentImportManager manager = new RedisIdempotentImportManager(redisTemplate);

        manager.clear("biz-unique");

        verify(redisTemplate).delete("import:idempotent:biz-unique");
    }

    @Configuration
    static class RedisStubConfig {

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return mock(StringRedisTemplate.class);
        }
    }
}
