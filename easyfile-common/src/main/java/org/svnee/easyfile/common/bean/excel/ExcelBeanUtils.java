package org.svnee.easyfile.common.bean.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.groups.Default;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.svnee.easyfile.common.annotation.ExcelProperty;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.util.CollectionUtils;

/**
 * @author svnee
 */
public final class ExcelBeanUtils {

    private ExcelBeanUtils() {
    }

    private static final Map<Class<?>, LinkedHashMap<Field, ExcelProperty>> GROUP_BY_FIELD_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * loadCache
     *
     * @param sourceClazz sourceClazz
     * @param cacheLoader cacheLoader
     * @return fieldCache
     */
    private static LinkedHashMap<Field, ExcelProperty> loadCache(Class<?> sourceClazz,
        Function<Class<?>, LinkedHashMap<Field, ExcelProperty>> cacheLoader) {
        LinkedHashMap<Field, ExcelProperty> resultMap = GROUP_BY_FIELD_CACHE_MAP.get(sourceClazz);
        if (Objects.isNull(resultMap)) {
            LinkedHashMap<Field, ExcelProperty> cacheMap = cacheLoader.apply(sourceClazz);
            GROUP_BY_FIELD_CACHE_MAP.put(sourceClazz, cacheMap);
            return cacheMap;
        }
        return resultMap;
    }

    /**
     * 按照分组获取所有的字段
     *
     * @return {@link List<Field>}
     */
    public static <T> List<Field> getFieldsByGroup(Class<T> sourceClazz, Class<?>... group) {
        Asserts.notNull(sourceClazz, CommonErrorCode.PARAM_ILLEGAL_ERROR);

        /////////// 加载缓存 /////////
        LinkedHashMap<Field, ExcelProperty> fieldCacheMap = loadCache(sourceClazz, sClazz -> {
            List<Field> fields = getClassFields(sClazz);
            return fields.stream()
                .filter(field -> {
                    ExcelProperty property = field.getDeclaredAnnotation(ExcelProperty.class);
                    if (Objects.nonNull(property)) {
                        return true;
                    }
                    return Stream.of(field.getDeclaredAnnotations())
                        .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(ExcelProperty.class));
                })
                .map(field -> Pair.of(field, AnnotatedElementUtils.findMergedAnnotation(field, ExcelProperty.class)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (v1, v2) -> v2, LinkedHashMap::new));
        });

        // 分组
        Set<Class<?>> groupSet;
        if (group == null || group.length == 0) {
            groupSet = new HashSet<>();
            groupSet.add(Default.class);
        } else {
            groupSet = new HashSet<>(CollectionUtils.newArrayList(group));
        }
        //获取所有的包含分组的字段
        return fieldCacheMap.entrySet().stream()
            .filter(e -> CollectionUtils
                .isNotEmpty(CollectionUtils.intersection(groupSet, CollectionUtils.newArrayList(e.getValue().group()))))
            .sorted(Comparator.comparingInt(o -> o.getValue().order()))
            .map(Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 获取父类的字段
     *
     * @return {@link List<Field>}
     */
    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Set<String> filedNameSet = new HashSet<>();
        Field[] fields;
        do {
            fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!filedNameSet.contains(field.getName())) {
                    list.add(field);
                    filedNameSet.add(field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);
        return list;
    }


}
