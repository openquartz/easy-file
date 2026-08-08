package com.openquartz.easyfile.core.parser;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入行包装：保留源行号、原始值与解析错误。
 *
 * @param <T> 行数据类型
 * @author svnee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRow<T> {

    /**
     * 数据行号（从 1 开始，不含表头）
     */
    private long rowNumber;

    /**
     * 映射后的业务对象；解析失败时可能为 null
     */
    private T data;

    /**
     * 原始单元格字符串值
     */
    private List<String> rawValues;

    /**
     * 行级解析错误；成功时为 null
     */
    private String parseError;
}
