package com.openquartz.easyfile.common.util;

import com.openquartz.easyfile.common.util.json.ClassJacksonHandler;
import com.openquartz.easyfile.common.util.json.GeneralJacksonHandler;
import com.openquartz.easyfile.common.util.json.JsonFacade;
import com.openquartz.easyfile.common.util.json.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * JSON util.
 *
 * @author svnee
 */
public final class JSONUtil {

    private static final JsonFacade JSON_FACADE = new GeneralJacksonHandler();
    private static final JsonFacade CLASS_JSON_FACADE = new ClassJacksonHandler();

    private JSONUtil() {
    }

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
     * json with class
     *
     * @param object obj
     * @return json
     */
    public static String toClassJson(Object object) {
        if (object == null) {
            return null;
        }

        return CLASS_JSON_FACADE.toJson(object);
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
     * 反序列化成对象
     *
     * @param text json
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    public static <T> T parseClassObject(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return CLASS_JSON_FACADE.parseObject(text, clazz);
    }

    /**
     * 反序列化成对象
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    public static <T> T parseObject(byte[] json, Class<T> clazz) {
        if (Objects.isNull(json) || json.length <= 0) {
            return null;
        }
        return JSON_FACADE.parseObject(json, clazz);
    }

    /**
     * 反序列化成对象
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    public static <T> T parseObject(byte[] json, TypeReference<T> clazz) {
        if (Objects.isNull(json) || json.length <= 0) {
            return null;
        }
        return JSON_FACADE.parseObject(json, clazz);
    }

    /**
     * 反序列化成对象
     *
     * @param text json
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    public static <T> T parseObject(String text, TypeReference<T> clazz) {
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
            return Collections.emptyList();
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
            return Collections.emptySet();
        }
        return JSON_FACADE.parseSet(json, clazz);
    }

    /**
     * toJsonAsBytes
     *
     * @param obj obj
     * @return byte[]
     */
    public static byte[] toJsonAsBytes(Object obj) {
        if (Objects.isNull(obj)) {
            return new byte[]{};
        }
        return JSON_FACADE.toJsonAsBytes(obj);
    }

}
