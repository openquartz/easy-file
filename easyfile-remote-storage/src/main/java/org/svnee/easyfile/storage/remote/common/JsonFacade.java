package org.svnee.easyfile.storage.remote.common;

import java.util.List;

/**
 * Json facade.
 *
 * @author svnee
 * @date 2021/12/13 20:01
 */
public interface JsonFacade {

    /**
     * To JSON string.
     */
    String toJSONString(Object object);

    /**
     * Parse object.
     */
    <T> T parseObject(String text, Class<T> clazz);

    /**
     * Parse array.
     */
    <T> List<T> parseArray(String text, Class<T> clazz);

}
