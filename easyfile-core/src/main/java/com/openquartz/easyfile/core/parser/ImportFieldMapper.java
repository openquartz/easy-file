package com.openquartz.easyfile.core.parser;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps raw cell values onto {@link ExcelImportField}-annotated types.
 */
@Slf4j
final class ImportFieldMapper {

    private ImportFieldMapper() {
    }

    static <T> ImportRow<T> mapRow(Class<T> targetClass, long rowNumber, List<String> rawValues) {
        ImportRow<T> importRow = ImportRow.<T>builder()
            .rowNumber(rowNumber)
            .rawValues(rawValues == null ? new ArrayList<>() : new ArrayList<>(rawValues))
            .build();
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                ExcelImportField annotation = field.getAnnotation(ExcelImportField.class);
                if (annotation == null) {
                    continue;
                }
                int columnIndex = annotation.columnIndex();
                if (columnIndex < 0 || rawValues == null || columnIndex >= rawValues.size()) {
                    continue;
                }
                String raw = rawValues.get(columnIndex);
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                field.setAccessible(true);
                field.set(instance, convertValue(raw.trim(), field.getType()));
            }
            importRow.setData(instance);
        } catch (Exception e) {
            log.warn("行解析失败: row={}, class={}", rowNumber, targetClass.getSimpleName(), e);
            importRow.setParseError(e.getMessage() == null ? "行解析失败" : e.getMessage());
        }
        return importRow;
    }

    static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            Number number = (Number) value;
            if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return number.intValue();
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return number.longValue();
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return number.doubleValue();
            } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return number.floatValue();
            } else if (targetType.equals(BigDecimal.class)) {
                if (value instanceof BigDecimal) {
                    return value;
                }
                return BigDecimal.valueOf(number.doubleValue());
            } else if (targetType.equals(String.class)) {
                // avoid 20.0 for integers stored as double
                if (number instanceof Double || number instanceof Float) {
                    double d = number.doubleValue();
                    if (d == Math.rint(d) && !Double.isInfinite(d)) {
                        return String.valueOf(number.longValue());
                    }
                }
                return value.toString();
            }
        }

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) {
            return null;
        }

        try {
            if (targetType.equals(String.class)) {
                return strValue;
            } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(stripTrailingZeroDecimal(strValue));
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(stripTrailingZeroDecimal(strValue));
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(strValue);
            } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return Float.parseFloat(strValue);
            } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return Boolean.parseBoolean(strValue);
            } else if (targetType.equals(BigDecimal.class)) {
                return new BigDecimal(strValue);
            } else if (targetType.equals(Date.class)) {
                if (value instanceof Date) {
                    return value;
                }
                return java.text.DateFormat.getInstance().parse(strValue);
            }
        } catch (Exception e) {
            log.warn("类型转换失败: value={}, targetType={}", value, targetType.getSimpleName());
            throw new IllegalArgumentException(
                "类型转换失败: value=" + value + ", targetType=" + targetType.getSimpleName(), e);
        }

        return value;
    }

    private static String stripTrailingZeroDecimal(String strValue) {
        if (strValue.contains(".")) {
            try {
                double d = Double.parseDouble(strValue);
                if (d == Math.rint(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
            } catch (NumberFormatException ignored) {
                // keep original
            }
        }
        return strValue;
    }
}
