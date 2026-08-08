package com.openquartz.easyfile.core.parser;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 导入解析器接口
 * 定义不同文件格式的解析器规范
 *
 * @author svnee
 */
public interface ImportParser<T> extends Closeable {

    /**
     * 获取总记录数（不含表头）
     *
     * @return 总记录数
     * @throws IOException 如果读取失败
     */
    long getTotalCount() throws IOException;

    /**
     * 从指定数据行开始读取一页
     *
     * @param startRow 起始数据行号（从 1 开始，不含表头）
     * @param pageSize 每页大小
     * @return 当前页的 {@link ImportRow} 列表
     * @throws IOException 如果读取失败
     */
    List<ImportRow<T>> readPage(long startRow, int pageSize) throws IOException;

    /**
     * 获取文件格式
     *
     * @return 文件格式 (xlsx, xls, csv 等)
     */
    String getFileType();

    /**
     * 关闭资源
     *
     * @throws IOException 如果关闭失败
     */
    @Override
    void close() throws IOException;
}
