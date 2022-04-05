package org.svnee.easyfile.common.util;

import java.util.List;
import java.util.Set;

/**
 * Json facade.
 *
 * @author svnee
 */
public interface JsonFacade {

    /**
     * To JSON string.
     *
     * @param object object
     * @return string
     */
    String toJson(Object object);

    /**
     * Parse object.
     *
     * @param text text
     * @param clazz clazz
     * @param <T> T
     * @return T
     */
    <T> T parseObject(String text, Class<T> clazz);

    /**
     * Type
     * @param text text
     * @param typeReference type
     * @param <T> T
     * @return T
     */
    <T> T parseObject(String text, TypeReference<T> typeReference);

    /**
     * Parse array.
     *
     * @param text text
     * @param clazz clazz
     * @param <T> T
     * @return T
     */
    <T> List<T> parseArray(String text, Class<T> clazz);

    /**
     * Parse Set
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return Set
     */
    <T> Set<T> parseSet(String json, Class<T> clazz);

}
