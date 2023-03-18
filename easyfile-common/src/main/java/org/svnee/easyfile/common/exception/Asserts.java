package org.svnee.easyfile.common.exception;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * 断言工具类
 *
 * @author svnee
 */
@Slf4j
public final class Asserts {

    private Asserts() {
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasyFileErrorCode code) {
        if (!expression) {
            throw new EasyFileException(code);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasyFileErrorCode code, Object... placeHold) {
        if (!expression) {
            throw EasyFileException.replacePlaceHold(code, placeHold);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param errorCode 异常码
     * @param exceptionClazz 异常类
     * @param <T> T 异常码
     * @param <E> E 异常
     */
    public static <T extends EasyFileErrorCode, E extends EasyFileException> void isTrue(boolean expression,
        T errorCode, Class<E> exceptionClazz) {
        if (!expression) {
            try {
                Constructor<E> constructor = exceptionClazz.getConstructor(errorCode.getClass());
                throw constructor.newInstance(errorCode);
            } catch (Exception ex) {
                throw new EasyFileException(CommonErrorCode.METHOD_NOT_EXIST_ERROR);
            }
        }
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasyFileErrorCode code) {
        if (obj == null) {
            throw new EasyFileException(code);
        }
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasyFileErrorCode code, Object... placeHold) {
        if (obj == null) {
            throw EasyFileException.replacePlaceHold(code, placeHold);
        }
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasyFileErrorCode code) {
        isTrue(Objects.isNull(obj), code);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasyFileErrorCode code, Object... placeHold) {
        isTrue(Objects.isNull(obj), code, placeHold);
    }

    /**
     * assert not empty
     *
     * @param obj obj
     * @param code code
     */
    public static void notEmpty(String obj, EasyFileErrorCode code, Object... placeHold) {
        isTrue(StringUtils.isNotBlank(obj), code, placeHold);
    }

    /**
     * assert not empty
     *
     * @param obj obj
     * @param code code
     */
    public static void notEmpty(Collection<?> obj, EasyFileErrorCode code, Object... placeHold) {
        isTrue(CollectionUtils.isNotEmpty(obj), code, placeHold);
    }

    /**
     * assert not empty
     *
     * @param obj obj
     * @param code code
     */
    public static void notEmpty(Map<?, ?> obj, EasyFileErrorCode code, Object... placeHold) {
        isTrue(MapUtils.isNotEmpty(obj), code, placeHold);
    }

}