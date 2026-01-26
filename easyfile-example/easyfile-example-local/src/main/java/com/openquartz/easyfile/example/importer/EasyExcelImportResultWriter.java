package com.openquartz.easyfile.example.importer;

import com.alibaba.excel.EasyExcel;
import com.openquartz.easyfile.common.bean.ImportErrorMsgAware;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.executor.writer.ImportResultWriter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * EasyExcel Import Result Writer
 *
 * @author svnee
 */
@Slf4j
@Component
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
            List<T> data = failures.stream().map(pair -> {
                T item = pair.getKey();
                String errorMsg = pair.getValue();
                setErrorMsg(item, errorMsg);
                return item;
            }).collect(Collectors.toList());

            if (!data.isEmpty()) {
                EasyExcel.write(file, data.get(0).getClass()).sheet("Errors").doWrite(data);
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

    private void setErrorMsg(Object target, String errorMsg) {
        if (target instanceof ImportErrorMsgAware) {
            ((ImportErrorMsgAware) target).setErrorMsg(errorMsg);
            return;
        }
        try {
            Method method = target.getClass().getMethod("setErrorMsg", String.class);
            method.invoke(target, errorMsg);
        } catch (NoSuchMethodException e) {
            // Ignore if no setter
            log.debug("No setErrorMsg method found for class: {}", target.getClass().getName());
        } catch (Exception e) {
            log.warn("Failed to set error message for class: {}", target.getClass().getName(), e);
        }
    }
}
