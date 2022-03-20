package org.svnee.easyfile.starter.intercept;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 拦截上下文
 *
 * @author svnee
 */
public final class InterceptorContext {

    /**
     * 上下文
     */
    private final Map<String, Object> contextMap = new HashMap<>();

    /**
     * Get Obj
     *
     * @param key key
     * @param clazz clazz
     * @param <T> T
     * @return obj
     */
    public <T> T get(String key, Class<T> clazz) {
        Object o = contextMap.get(key);
        if (Objects.isNull(o)) {
            return null;
        }
        return (T) o;
    }

    /**
     * set context
     *
     * @param key key
     * @param obj obj
     * @param <T> T
     */
    public <T> void set(String key, T obj) {
        contextMap.put(key, obj);
    }

    /**
     * 新实例
     *
     * @return 拦截上下文
     */
    public static InterceptorContext newInstance() {
        return new InterceptorContext();
    }
}
