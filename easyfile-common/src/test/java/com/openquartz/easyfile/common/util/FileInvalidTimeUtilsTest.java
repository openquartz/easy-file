package com.openquartz.easyfile.common.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Tests for export file invalid-time calculation.
 */
public class FileInvalidTimeUtilsTest {

    @Test
    public void shouldNotReturnNowAsInvalidTime() {
        Date before = new Date();
        Date invalidTime = FileInvalidTimeUtils.calculateDefaultInvalidTime();
        assertNotNull(invalidTime);
        // default TTL is hours ahead; must be strictly after "now"
        assertTrue(invalidTime.getTime() - before.getTime() > TimeUnit.HOURS.toMillis(1));
    }

    @Test
    public void shouldUseConfiguredHoursForFileSystem() {
        Date before = new Date();
        Map<String, Integer> map = new HashMap<>();
        map.put("OSS", 2);
        Date invalidTime = FileInvalidTimeUtils.calculateInvalidTime("OSS", map);
        long delta = invalidTime.getTime() - before.getTime();
        assertTrue(delta >= TimeUnit.HOURS.toMillis(2) - 5000);
        assertTrue(delta <= TimeUnit.HOURS.toMillis(2) + 5000);
    }

    @Test
    public void shouldFallbackToDefaultWhenSystemMissing() {
        Date before = new Date();
        Date invalidTime = FileInvalidTimeUtils.calculateInvalidTime("UNKNOWN", Collections.emptyMap());
        assertTrue(invalidTime.getTime() - before.getTime()
            > TimeUnit.HOURS.toMillis(FileInvalidTimeUtils.DEFAULT_INVALID_HOURS - 1));
    }

    @Test
    public void shouldFallbackWhenConfiguredHoursIsNull() {
        Map<String, Integer> map = new HashMap<>();
        map.put("OSS", null);
        Date before = new Date();
        Date invalidTime = FileInvalidTimeUtils.calculateInvalidTime("OSS", map);
        assertTrue(invalidTime.getTime() - before.getTime()
            > TimeUnit.HOURS.toMillis(FileInvalidTimeUtils.DEFAULT_INVALID_HOURS - 1));
    }
}
