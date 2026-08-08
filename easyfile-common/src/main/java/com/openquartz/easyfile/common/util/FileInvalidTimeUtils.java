package com.openquartz.easyfile.common.util;

import com.openquartz.easyfile.common.constants.Constants;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Calculate export file invalid time.
 *
 * @author svnee
 */
public final class FileInvalidTimeUtils {

    /**
     * Default TTL hours when filesystem is not configured (aligned with server default).
     */
    public static final int DEFAULT_INVALID_HOURS = 100;

    private FileInvalidTimeUtils() {
    }

    public static Date calculateDefaultInvalidTime() {
        return calculateInvalidTime(null, null);
    }

    /**
     * @param fileSystem file system code
     * @param fileInvalidTimeMap hours by filesystem; null/empty uses {@link #DEFAULT_INVALID_HOURS}
     * @return invalid time = now + hours
     */
    public static Date calculateInvalidTime(String fileSystem, Map<String, Integer> fileInvalidTimeMap) {
        int hours = DEFAULT_INVALID_HOURS;
        if (fileInvalidTimeMap != null && fileSystem != null) {
            Integer configured = fileInvalidTimeMap.get(fileSystem);
            if (configured != null) {
                hours = configured;
            }
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8));
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }
}
