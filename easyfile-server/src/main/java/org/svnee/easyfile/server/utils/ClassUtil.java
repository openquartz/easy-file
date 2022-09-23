package org.svnee.easyfile.server.utils;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.reflections.Reflections;

/**
 * 枚举注册工具类
 *
 * @author svnee
 */
public final class ClassUtil {

    private ClassUtil() {
    }

    /**
     * 获取当前类的所有的子类枚举对象
     *
     * @param superClass 父类对象
     * @param packageDir 包路径
     * @param <T> 父类
     * @return 所有子类对象枚举
     */
    public static <T> Set<Class<? extends T>> getAllSubEnumClass(Class<T> superClass, String... packageDir) {
        Set<Class<? extends T>> subClazzSet = Sets.newHashSet();
        for (String s : packageDir) {
            Reflections reflections = new Reflections(s, Thread.currentThread().getContextClassLoader());
            Set<Class<? extends T>> classSet = reflections.getSubTypesOf(superClass);
            Optional.ofNullable(classSet)
                .ifPresent(classes -> classes.stream().filter(Class::isEnum)
                    .forEach(subClazzSet::add));
        }
        return subClazzSet;
    }

}
