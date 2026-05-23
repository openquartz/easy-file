package com.openquartz.easyfile.core.executor.support;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 文件导入执行器支持
 *
 * @author svnee
 */
public final class FileImportExecutorSupport {

    private FileImportExecutorSupport() {
    }

    /**
     * 导入器缓存
     */
    private static final Map<String, Pair<FileImportExecutor, BaseImportExecutor<?>>> BASE_IMPORT_EXECUTOR_MAP;

    static {
        BASE_IMPORT_EXECUTOR_MAP = MapUtils.newHashMapWithExpectedSize(10);
    }

    /**
     * 注册
     *
     * @param importCode 导入code
     * @param importExecutor 注解
     * @param executor 导入执行器
     */
    public static void register(
        String importCode,
        FileImportExecutor importExecutor,
        BaseImportExecutor<?> executor
    ) {
        BASE_IMPORT_EXECUTOR_MAP.putIfAbsent(importCode, Pair.of(importExecutor, executor));
    }

    /**
     * 获取执行器
     *
     * @param importCode 导入code
     * @return 导入执行器
     */
    public static BaseImportExecutor<?> get(String importCode) {
        return BASE_IMPORT_EXECUTOR_MAP.get(importCode).getValue();
    }

    /**
     * 是否包含导入code
     *
     * @param importCode 导入code
     * @return 是否包含
     */
    public static boolean contains(String importCode) {
        return BASE_IMPORT_EXECUTOR_MAP.containsKey(importCode);
    }

    /**
     * 导入code集合
     *
     * @return codeList
     */
    public static List<String> importCodeList() {
        return new ArrayList<>(BASE_IMPORT_EXECUTOR_MAP.keySet());
    }

    /**
     * 查询导入码
     *
     * @return key：导入code, value: 导入描述
     */
    public static Map<String, String> getImportCodeMap() {
        return BASE_IMPORT_EXECUTOR_MAP.entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getKey().desc()));
    }

    /**
     * 查询执行器
     */
    public static List<BaseImportExecutor<?>> executorList() {
        return BASE_IMPORT_EXECUTOR_MAP.values().stream().map(Pair::getValue).collect(Collectors.toList());
    }
}
