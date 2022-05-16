package org.svnee.easyfile.common.bean.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.groups.Default;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.svnee.easyfile.common.annotation.ExcelProperty;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.util.CollectionUtils;

/**
 * @author svnee
 */
public final class ExcelBeanUtils {

    private ExcelBeanUtils() {
    }

    private static final Map<Class<?>, LinkedHashSet<ExcelFiled>> GROUP_BY_FIELD_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * loadCache
     *
     * @param sourceClazz sourceClazz
     * @param cacheLoader cacheLoader
     * @return fieldCache
     */
    private static LinkedHashSet<ExcelFiled> loadCache(Class<?> sourceClazz,
        Function<Class<?>, LinkedHashSet<ExcelFiled>> cacheLoader) {
        LinkedHashSet<ExcelFiled> resultMap = GROUP_BY_FIELD_CACHE_MAP.get(sourceClazz);
        if (Objects.isNull(resultMap)) {
            LinkedHashSet<ExcelFiled> cacheSet = cacheLoader.apply(sourceClazz);
            GROUP_BY_FIELD_CACHE_MAP.put(sourceClazz, cacheSet);
            return cacheSet;
        }
        return resultMap;
    }

    /**
     * 按照分组获取导出工具字段类
     *
     * @param sourceClazz class
     * @param group 分组
     * @param <T> T
     * @return excel 字段
     */
    public static <T> List<ExcelFiled> getExcelFiledByGroup(Class<T> sourceClazz, Class<?>... group) {
        Asserts.notNull(sourceClazz, CommonErrorCode.PARAM_ILLEGAL_ERROR);

        /////////// 加载缓存 /////////
        LinkedHashSet<ExcelFiled> excelFiledList = loadCache(sourceClazz, sClazz -> {
            List<Field> fields = getClassFields(sClazz);

            return fields.stream()
                .map(field -> {
                    ExcelProperty excelProperty = getAnnotatedExcelProperty(field);
                    if (Objects.isNull(excelProperty)) {
                        return null;
                    }
                    ExcelFiled excelFiled = new ExcelFiled(field, excelProperty);
                    if (!ReflectionUtils.isJavaClass(field.getType())) {
                        List<ExcelFiled> subFieldList = getClassFields(field.getType())
                            .stream()
                            .map(e -> {
                                ExcelProperty subProperty = getAnnotatedExcelProperty(e);
                                if (Objects.nonNull(subProperty)) {
                                    return new ExcelFiled(e, subProperty);
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .sorted((Comparator.comparingInt(o -> o.getExcelProperty().order())))
                            .collect(Collectors.toList());
                        excelFiled.setSubFiledList(subFieldList);
                        if (CollectionUtils.isEmpty(subFieldList)) {
                            return null;
                        }
                    }
                    return excelFiled;
                })
                .filter(Objects::nonNull)
                .sorted((Comparator.comparingInt(o -> o.getExcelProperty().order())))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
        return excelFiledList.stream()
            .filter(e -> {
                // 如果当前符合条件了直接进行判断子字段是否符合
                boolean groupField = isBelongGroup(e, groupSet);
                if (groupField) {
                    List<ExcelFiled> subFieldList = e.getSubFiledList().stream()
                        .filter(subFiled -> isBelongGroup(subFiled, groupSet)).collect(Collectors.toList());
                    e.setSubFiledList(subFieldList);
                }
                return groupField;
            })
            .sorted(Comparator.comparingInt(o -> o.getExcelProperty().order()))
            .collect(Collectors.toList());
    }

    private static boolean isBelongGroup(ExcelFiled filed, Set<?> groupSet) {
        return CollectionUtils
            .isNotEmpty(
                CollectionUtils.intersection(groupSet, CollectionUtils.newArrayList(filed.getExcelProperty().group())));
    }

    /**
     * 获取被ExcelProperty注解标注的注解
     *
     * @param field 属性字段
     * @return ExcelProperty注解
     */
    private static ExcelProperty getAnnotatedExcelProperty(Field field) {
        ExcelProperty property = field.getDeclaredAnnotation(ExcelProperty.class);
        if (Objects.nonNull(property)) {
            return property;
        }
        return AnnotatedElementUtils.findMergedAnnotation(field, ExcelProperty.class);
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
