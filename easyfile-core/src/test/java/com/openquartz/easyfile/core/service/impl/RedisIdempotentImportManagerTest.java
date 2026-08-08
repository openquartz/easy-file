package com.openquartz.easyfile.core.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.response.ImportResult;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * RedisIdempotentImportManager unit tests.
 */
public class RedisIdempotentImportManagerTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisIdempotentImportManager manager;

    @Before
    public void setUp() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        manager = new RedisIdempotentImportManager(redisTemplate);
    }

    @Test
    public void markAndClear_roundTrip() {
        ImportResult result = ImportResult.builder().success(true).status("partial_success").build();
        manager.markAsExecuted("biz-1", result);
        verify(valueOperations).set(eq("import:idempotent:biz-1"), anyString(), eq(Duration.ofDays(30)));

        when(redisTemplate.hasKey("import:idempotent:biz-1")).thenReturn(true);
        assertTrue(manager.isExecuted("biz-1"));

        manager.clear("biz-1");
        verify(redisTemplate).delete("import:idempotent:biz-1");

        when(redisTemplate.hasKey("import:idempotent:biz-1")).thenReturn(false);
        assertFalse(manager.isExecuted("biz-1"));
    }

    @Test
    public void clear_ignoresBlankKey() {
        manager.clear(null);
        manager.clear("");
        verify(redisTemplate, never()).delete(anyString());
    }
}
