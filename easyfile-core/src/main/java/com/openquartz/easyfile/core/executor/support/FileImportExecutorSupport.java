package com.openquartz.easyfile.core.executor.support;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.executor.BaseAsyncImportExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * File Import Executor Support
 *
 * @author svnee
 */
public final class FileImportExecutorSupport {

    private FileImportExecutorSupport() {
    }

    private static final Map<String, Pair<FileImportExecutor, BaseAsyncImportExecutor<?>>> EXECUTOR_MAP;

    static {
        EXECUTOR_MAP = MapUtils.newHashMapWithExpectedSize(10);
    }

    public static void register(String code, FileImportExecutor annotation, BaseAsyncImportExecutor<?> executor) {
        EXECUTOR_MAP.putIfAbsent(code, Pair.of(annotation, executor));
    }

    @SuppressWarnings("unchecked")
    public static <T>BaseAsyncImportExecutor<T> get(String code) {
        Pair<FileImportExecutor, BaseAsyncImportExecutor<?>> pair = EXECUTOR_MAP.get(code);
        return pair != null ? (BaseAsyncImportExecutor<T>)pair.getValue() : null;
    }

    public static boolean contains(String code) {
        return EXECUTOR_MAP.containsKey(code);
    }
    
    public static List<String> getCodeList() {
        return new ArrayList<>(EXECUTOR_MAP.keySet());
    }
    
    public static Map<String, String> getCodeMap() {
        return EXECUTOR_MAP.entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getKey().desc()));
    }
}
