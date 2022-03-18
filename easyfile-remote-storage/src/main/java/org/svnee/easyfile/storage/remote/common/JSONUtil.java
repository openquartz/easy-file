package org.svnee.easyfile.storage.remote.common;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON util.
 *
 * @author svnee
 */
public class JSONUtil {

    private static final JsonFacade JSON_FACADE = new JacksonHandler();

    public static String toJSONString(Object object) {
        if (object == null) {
            return null;
        }

        return JSON_FACADE.toJSONString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return JSON_FACADE.parseObject(text, clazz);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return JSON_FACADE.parseArray(text, clazz);
    }

}
