package com.openquartz.easyfile.starter.executor.impl;

import com.alibaba.excel.EasyExcel;
import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.common.bean.ImportErrorMsgAware;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.executor.writer.ImportResultWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * EasyExcel Import Result Writer with support for custom @ExcelProperty
 *
 * @author svnee
 */
@Slf4j
public class EasyExcelImportResultWriter implements ImportResultWriter {

    @Override
    public boolean support(String fileName) {
        return fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"));
    }

    @Override
    public <T> File writeErrorFile(List<Pair<T, String>> failures, String originalFileName) {
        if (failures == null || failures.isEmpty()) {
            return null;
        }

        String suffix = originalFileName.contains(".") ? 
            originalFileName.substring(originalFileName.lastIndexOf(".")) : ".xlsx";
        String fileName = UUID.randomUUID() + "_error" + suffix;
        File file = new File(fileName);

        try {
            // Prepare data and set error message
            List<T> data = failures.stream().map(pair -> {
                T item = pair.getKey();
                String errorMsg = pair.getValue();
                setErrorMsg(item, errorMsg);
                return item;
            }).collect(Collectors.toList());

            if (data.isEmpty()) {
                return null;
            }

            Class<?> clazz = data.get(0).getClass();
            
            // Check for custom annotation
            boolean usesCustomAnnotation = false;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ExcelProperty.class)) {
                    usesCustomAnnotation = true;
                    break;
                }
            }

            if (usesCustomAnnotation) {
                writeWithCustomAnnotation(file, data, clazz);
            } else {
                EasyExcel.write(file, clazz).sheet("Errors").doWrite(data);
            }
            
            return file;
        } catch (Exception e) {
            log.error("Failed to write error file", e);
            if (file.exists()) {
                file.delete();
            }
            return null;
        }
    }

    private <T> void writeWithCustomAnnotation(File file, List<T> data, Class<?> clazz) {
        List<Field> sortedFields = new ArrayList<>();
        ReflectionUtils.doWithFields(clazz, field -> {
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                field.setAccessible(true);
                sortedFields.add(field);
            }
        });
        
        // Sort by order
        sortedFields.sort(Comparator.comparingInt(f -> f.getAnnotation(ExcelProperty.class).order()));

        // Build Heads
        List<List<String>> head = new ArrayList<>();
        for (Field field : sortedFields) {
            String value = field.getAnnotation(ExcelProperty.class).value();
            head.add(Collections.singletonList(StringUtils.hasText(value) ? value : field.getName()));
        }

        // Build Data
        List<List<Object>> rows = new ArrayList<>();
        for (T item : data) {
            List<Object> row = new ArrayList<>();
            for (Field field : sortedFields) {
                try {
                    row.add(field.get(item));
                } catch (IllegalAccessException e) {
                    row.add(null);
                }
            }
            rows.add(row);
        }

        EasyExcel.write(file).head(head).sheet("Errors").doWrite(rows);
    }

    private void setErrorMsg(Object target, String errorMsg) {
        if (target instanceof ImportErrorMsgAware) {
            ((ImportErrorMsgAware) target).setErrorMsg(errorMsg);
            return;
        }
        try {
            Method method = target.getClass().getMethod("setErrorMsg", String.class);
            method.invoke(target, errorMsg);
        } catch (NoSuchMethodException e) {
            log.debug("No setErrorMsg method found for class: {}", target.getClass().getName());
        } catch (Exception e) {
            log.warn("Failed to set error message for class: {}", target.getClass().getName(), e);
        }
    }
}
