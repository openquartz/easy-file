package com.openquartz.easyfile.core.executor.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;

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
    private static final Map<String, Pair<FileExportExecutor, BaseDownloadExecutor>> BASE_DOWNLOAD_EXECUTOR_MAP;

    static {
        BASE_DOWNLOAD_EXECUTOR_MAP = MapUtils.newHashMapWithExpectedSize(10);
    }

    /**
     * 注册
     *
     * @param downloadCode 缓存code
     * @param executor 下载器
     */
    public static void register(String downloadCode, FileExportExecutor exportExecutor, BaseDownloadExecutor executor) {
        BASE_DOWNLOAD_EXECUTOR_MAP.putIfAbsent(downloadCode, Pair.of(exportExecutor, executor));
    }

    /**
     * 获取code
     *
     * @param downloadCode code
     * @return 下载器
     */
    public static BaseDownloadExecutor get(String downloadCode) {
        return BASE_DOWNLOAD_EXECUTOR_MAP.get(downloadCode).getValue();
    }

    /**
     * 是否包含下载code
     *
     * @param downloadCode 下载code
     * @return 是否包含
     */
    public static boolean contains(String downloadCode) {
        return BASE_DOWNLOAD_EXECUTOR_MAP.containsKey(downloadCode);
    }

    /**
     * 下载code集合
     *
     * @return codeList
     */
    public static List<String> downloadCodeList() {
        return new ArrayList<>(BASE_DOWNLOAD_EXECUTOR_MAP.keySet());
    }

    /**
     * 查询下载码
     *
     * @return 下载code key：下载code --> value: 下载描述
     */
    public static Map<String, String> getDownloadCodeMap() {
        return BASE_DOWNLOAD_EXECUTOR_MAP.entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getKey().desc()));
    }

    /**
     * 查询执行器
     */
    public static List<BaseDownloadExecutor> executorList() {
        return BASE_DOWNLOAD_EXECUTOR_MAP.values().stream().map(Pair::getValue).collect(Collectors.toList());
    }
}
