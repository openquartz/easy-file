package com.openquartz.easyfile.core.executor.writer;

import com.openquartz.easyfile.common.bean.Pair;
import java.io.File;
import java.util.List;

/**
 * 导入结果写入器
 *
 * @author svnee
 */
public interface ImportResultWriter {

    /**
     * 是否支持
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean support(String fileName);

    /**
     * 写入错误文件
     *
     * @param failures 失败列表
     * @param originalFileName 原始文件名
     * @return 错误文件
     */
    <T> File writeErrorFile(List<Pair<T, String>> failures, String originalFileName);
}
