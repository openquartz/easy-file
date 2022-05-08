package org.svnee.easyfile.common.dictionary;

/**
 * 基础枚举类定义
 *
 * @param <T> T
 * @author svnee
 */
public interface BaseEnum<T> {

    /**
     * code
     *
     * @return enum code
     */
    T getCode();

    /**
     * desc
     *
     * @return enum desc
     */
    String getDesc();
}
