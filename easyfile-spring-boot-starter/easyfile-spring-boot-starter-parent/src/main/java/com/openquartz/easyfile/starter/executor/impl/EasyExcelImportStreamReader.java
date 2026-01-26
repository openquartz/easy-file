package com.openquartz.easyfile.starter.executor.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.core.executor.reader.ImportStreamReader;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * EasyExcel Import Reader Implementation with support for custom @ExcelProperty
 *
 * @author svnee
 */
@Slf4j
public class EasyExcelImportStreamReader implements ImportStreamReader {

    @Override
    public boolean support(String fileName) {
        return fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"));
    }

    @Override
    public <T> void read(InputStream inputStream, Class<T> clazz, int batchSize, Consumer<List<T>> consumer) {
        // Check if the class uses our custom @ExcelProperty
        boolean usesCustomAnnotation = false;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                usesCustomAnnotation = true;
                break;
            }
        }

        if (usesCustomAnnotation) {
            log.info("Using custom @ExcelProperty logic for class: {}", clazz.getName());
            readWithCustomAnnotation(inputStream, clazz, batchSize, consumer);
        } else {
            log.info("Using standard EasyExcel logic for class: {}", clazz.getName());
            readStandard(inputStream, clazz, batchSize, consumer);
        }
    }

    private <T> void readStandard(InputStream inputStream, Class<T> clazz, int batchSize, Consumer<List<T>> consumer) {
        EasyExcel.read(inputStream, clazz, new AnalysisEventListener<T>() {
            private List<T> cachedDataList = new ArrayList<>(batchSize);

            @Override
            public void invoke(T data, AnalysisContext context) {
                cachedDataList.add(data);
                if (cachedDataList.size() >= batchSize) {
                    consumer.accept(cachedDataList);
                    cachedDataList = new ArrayList<>(batchSize);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                if (!cachedDataList.isEmpty()) {
                    consumer.accept(cachedDataList);
                }
            }
        }).sheet().doRead();
    }

    private <T> void readWithCustomAnnotation(InputStream inputStream, Class<T> clazz, int batchSize, Consumer<List<T>> consumer) {
        EasyExcel.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
            private List<T> cachedDataList = new ArrayList<>(batchSize);
            private final Map<Integer, Field> indexFieldMap = new HashMap<>();
            private final Map<String, Field> nameFieldMap = new HashMap<>();
            private final Map<Integer, Field> finalMapping = new HashMap<>();
            private final Map<String, String> dateFormatMap = new HashMap<>();

            {
                // Pre-process fields
                ReflectionUtils.doWithFields(clazz, field -> {
                    ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                    if (annotation != null) {
                        field.setAccessible(true);
                        if (annotation.order() != Integer.MAX_VALUE) {
                            indexFieldMap.put(annotation.order(), field);
                        }
                        if (StringUtils.hasText(annotation.value())) {
                            nameFieldMap.put(annotation.value(), field);
                        }
                        if (StringUtils.hasText(annotation.dateFormatter())) {
                            dateFormatMap.put(field.getName(), annotation.dateFormatter());
                        }
                    }
                });
            }

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                // Map column index to field based on head names
                for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                    Integer index = entry.getKey();
                    String name = entry.getValue();
                    
                    // Priority 1: Order (already in indexFieldMap)
                    if (indexFieldMap.containsKey(index)) {
                        finalMapping.put(index, indexFieldMap.get(index));
                    } 
                    // Priority 2: Name match
                    else if (nameFieldMap.containsKey(name)) {
                        finalMapping.put(index, nameFieldMap.get(name));
                    }
                }
                
                // Also add any fixed order fields that might not have been matched by name
                indexFieldMap.forEach(finalMapping::putIfAbsent);
            }

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                try {
                    T instance = clazz.newInstance();
                    BeanWrapper beanWrapper = new BeanWrapperImpl(instance);
                    
                    // If finalMapping is empty (maybe invokeHeadMap wasn't called or no matches), 
                    // try to use indexFieldMap directly
                    if (finalMapping.isEmpty() && !indexFieldMap.isEmpty()) {
                         indexFieldMap.forEach(finalMapping::put);
                    }

                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        Integer index = entry.getKey();
                        String value = entry.getValue();
                        
                        Field field = finalMapping.get(index);
                        if (field != null) {
                            // Handle Date format if specified
                            if (dateFormatMap.containsKey(field.getName()) && (field.getType() == Date.class)) {
                                String pattern = dateFormatMap.get(field.getName());
                                beanWrapper.registerCustomEditor(Date.class, field.getName(), 
                                    new CustomDateEditor(new SimpleDateFormat(pattern), true));
                            }
                            
                            try {
                                beanWrapper.setPropertyValue(field.getName(), value);
                            } catch (Exception e) {
                                log.warn("Failed to set property {} with value {}", field.getName(), value, e);
                            }
                        }
                    }
                    
                    cachedDataList.add(instance);
                    if (cachedDataList.size() >= batchSize) {
                        consumer.accept(cachedDataList);
                        cachedDataList = new ArrayList<>(batchSize);
                    }
                } catch (Exception e) {
                    log.error("Error creating instance of {}", clazz.getName(), e);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                if (!cachedDataList.isEmpty()) {
                    consumer.accept(cachedDataList);
                }
            }
        }).sheet().doRead();
    }
}
