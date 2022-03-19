package org.svnee.easyfile.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据时间格式化
 *
 * @author svnee
 **/
public final class DateFormatUtils {

    private static final ThreadLocal<Map<String, SimpleDateFormat>> SIMPLE_DATE_FORMAT = new ThreadLocal<>();

    public static String format(Date date, String pattern) {
        Map<String, SimpleDateFormat> formatMap = SIMPLE_DATE_FORMAT.get();
        if (Objects.isNull(formatMap)) {
            formatMap = MapUtils.newHashMapWithExpectedSize(10);
            formatMap.putIfAbsent(pattern, new SimpleDateFormat(pattern));
        } else if (!formatMap.containsKey(pattern)) {
            formatMap.putIfAbsent(pattern, new SimpleDateFormat(pattern));
        }
        SIMPLE_DATE_FORMAT.set(formatMap);
        return formatMap.get(pattern).format(date);
    }

}
