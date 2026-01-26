package com.openquartz.easyfile.example.importer;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.openquartz.easyfile.core.executor.reader.ImportStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * EasyExcel Import Reader Implementation
 *
 * @author svnee
 */
@Slf4j
@Component
public class EasyExcelImportStreamReader implements ImportStreamReader {

    @Override
    public boolean support(String fileName) {
        return fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"));
    }

    @Override
    public <T> void read(InputStream inputStream, Class<T> clazz, int batchSize, Consumer<List<T>> consumer) {
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
}
