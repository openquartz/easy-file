package com.openquartz.easyfile.core.parser;

import java.io.IOException;
import java.util.List;

/**
 * 导入解析器接口
 * 定义不同文件格式的解析器规范
 *
 * @author svnee
 */
public interface ImportParser<T> {

    /**
     * 获取总记录数
     *
     * @return 总记录数
     * @throws IOException 如果读取失败
     */
    long getTotalCount() throws IOException;

    /**
     * 读取分页数据
     *
     * @param pageNum 页码 (从1开始)
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     * @throws IOException 如果读取失败
     */
    List<T> readPage(int pageNum, int pageSize) throws IOException;

    /**
     * 获取文件格式
     *
     * @return 文件格式 (xlsx, xls, csv, json等)
     */
    String getFileType();

    /**
     * 关闭资源
     *
     * @throws IOException 如果关闭失败
     */
    void close() throws IOException;
}