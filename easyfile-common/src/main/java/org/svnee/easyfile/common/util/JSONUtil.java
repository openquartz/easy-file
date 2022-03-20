package org.svnee.easyfile.common.util;

import java.util.List;
import java.util.Set;

/**
 * JSON util.
 *
 * @author svnee
 */
public class JSONUtil {

    private static final JsonFacade JSON_FACADE = new JacksonHandler();

    /**
     * 返回Json
     *
     * @param object obj
     * @return json
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }

        return JSON_FACADE.toJson(object);
    }

    /**
     * 反序列化成对象
     *
     * @param text json
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return JSON_FACADE.parseObject(text, clazz);
    }

    /**
     * 解析数组
     *
     * @param text json
     * @param clazz clazz
     * @param <T> T
     * @return 返回ArrayList
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return JSON_FACADE.parseArray(text, clazz);
    }

    /**
     * Parse Set
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return Set
     */
    public static <T> Set<T> parseSet(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON_FACADE.parseSet(json, clazz);
    }


}
