package com.openquartz.easyfile.core.executor.reader;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * 导入流读取器
 *
 * @author svnee
 */
public interface ImportStreamReader {

    /**
     * 是否支持
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean support(String fileName);

    /**
     * 读取
     *
     * @param inputStream 输入流
     * @param clazz 类
     * @param batchSize 批次大小
     * @param consumer 消费者
     * @param <T> T
     */
    <T> void read(InputStream inputStream, Class<T> clazz, int batchSize, Consumer<List<T>> consumer);
}
