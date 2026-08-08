package com.openquartz.easyfile.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Tests for export cache key matching (safe SpEL).
 */
public class CacheKeyMatcherTest {

    @Test
    public void emptyCacheKeyListShouldMatch() {
        assertTrue(CacheKeyMatcher.match(Collections.emptyList(), map("age", 1), map("age", 2)));
        assertTrue(CacheKeyMatcher.match(null, map("age", 1), map("age", 2)));
    }

    @Test
    public void equalMapsShouldMatchWithoutEvaluatingKeys() {
        Map<String, Object> map = map("age", 18);
        assertTrue(CacheKeyMatcher.match(Collections.singletonList("age"), map, new HashMap<>(map)));
    }

    @Test
    public void shouldMatchSimpleVariableKey() {
        Map<String, Object> ori = map("age", 18);
        Map<String, Object> tar = map("age", 18);
        assertTrue(CacheKeyMatcher.match(Collections.singletonList("age"), ori, tar));
        assertFalse(CacheKeyMatcher.match(Collections.singletonList("age"), ori, map("age", 19)));
    }

    @Test
    public void shouldMatchNestedSpelKey() {
        Map<String, Object> request = map("id", 1L);
        Map<String, Object> ori = new HashMap<>();
        ori.put("request", request);
        Map<String, Object> tar = new HashMap<>();
        tar.put("request", map("id", 1L));
        assertTrue(CacheKeyMatcher.match(Collections.singletonList("#request.id"), ori, tar));
        tar.put("request", map("id", 2L));
        assertFalse(CacheKeyMatcher.match(Collections.singletonList("#request.id"), ori, tar));
    }

    @Test
    public void shouldRejectTypeReferenceRceExpressions() {
        // maps must differ so SpEL is evaluated (equal maps short-circuit)
        Map<String, Object> ori = map("age", 1);
        Map<String, Object> tar = map("age", 2);
        // T(...) is blocked by SimpleEvaluationContext → mismatch/false, not RCE
        assertFalse(CacheKeyMatcher.match(
            Collections.singletonList("T(java.lang.Runtime).getRuntime().exec('true')"),
            ori, tar));
    }

    @Test
    public void shouldMatchMultipleKeys() {
        Map<String, Object> ori = new HashMap<>();
        ori.put("age", 18);
        ori.put("name", "a");
        Map<String, Object> tar = new HashMap<>();
        tar.put("age", 18);
        tar.put("name", "a");
        assertTrue(CacheKeyMatcher.match(Arrays.asList("age", "name"), ori, tar));
        tar.put("name", "b");
        assertFalse(CacheKeyMatcher.match(Arrays.asList("age", "name"), ori, tar));
    }

    private static Map<String, Object> map(String k, Object v) {
        Map<String, Object> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
