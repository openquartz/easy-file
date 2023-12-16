package com.openquartz.easyfile.core.executor.support;

import com.openquartz.easyfile.core.executor.BaseExportExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.core.executor.Executor;

/**
 * 文件导出执行器 支持
 *
 * @author svnee
 */
public final class FileExportExecutorSupport {

    private FileExportExecutorSupport() {
    }

    /**
     * 下载器缓存
     */
    private static final Map<String, Executor> BASE_EXECUTOR_MAP;

    static {
        BASE_EXECUTOR_MAP = MapUtils.newHashMapWithExpectedSize(16);
    }

    /**
     * 注册
     *
     * @param downloadCode 缓存code
     * @param executor     下载器
     */
    @SuppressWarnings("all")
    public static <T extends Executor> void register(String downloadCode, T executor) {
        BASE_EXECUTOR_MAP.putIfAbsent(downloadCode, executor);
    }

    /**
     * 获取code
     *
     * @param downloadCode code
     * @return 下载器
     */
    public static Executor get(String downloadCode) {
        return BASE_EXECUTOR_MAP.get(downloadCode);
    }

    /**
     * 是否包含下载code
     *
     * @param downloadCode 下载code
     * @return 是否包含
     */
    public static boolean contains(String downloadCode) {
        return BASE_EXECUTOR_MAP.containsKey(downloadCode);
    }

    /**
     * 下载code集合
     *
     * @return codeList
     */
    public static List<String> downloadCodeList() {
        return new ArrayList<>(BASE_EXECUTOR_MAP.keySet());
    }

    /**
     * 查询执行器
     */
    public static List<Executor> executorList() {
        return new ArrayList<>(BASE_EXECUTOR_MAP.values());
    }
}
