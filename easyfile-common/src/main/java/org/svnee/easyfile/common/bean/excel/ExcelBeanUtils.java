package org.svnee.easyfile.common.bean.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.groups.Default;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.svnee.easyfile.common.annotation.ExcelProperty;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.util.CollectionUtils;

/**
 * @author svnee
 * @date 2019/10/11 11:53
 */
public final class ExcelBeanUtils {

    private ExcelBeanUtils() {
    }

    /**
     * 按照分组获取所有的字段
     *
     * @return {@link List< Field>}
     * @date 2019/9/19 17:34
     */
    public static <T> List<Field> getFieldsByGroup(Class<T> sourceClazz, Class<?>... group) {
        org.springframework.util.Assert.notNull(sourceClazz, "source class must not be null");
        List<Field> fields = getClassFields(sourceClazz);
        // 分组
        Set<Class<?>> groupSet;
        if (group == null || group.length == 0) {
            groupSet = new HashSet<>();
            groupSet.add(Default.class);
        } else {
            groupSet = new HashSet<>(Arrays.asList(group));
        }

        //获取所有的包含分组的字段
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
            .filter(e -> CollectionUtils
                .isNotEmpty(CollectionUtils.intersection(groupSet, Arrays.asList(e.getValue().group()))))
            .sorted(Comparator.comparingInt(o -> o.getValue().order()))
            .map(Pair::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 获取父类的字段
     *
     * @return {@link List<Field>}
     * @date 2019/9/23 14:23
     */
    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Set<String> filedNameSet = new HashSet<>();
        Field[] fields;
        do {
            fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (!filedNameSet.contains(fields[i].getName())) {
                    list.add(fields[i]);
                    filedNameSet.add(fields[i].getName());
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);
        return list;
    }


}
