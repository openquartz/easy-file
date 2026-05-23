package com.openquartz.easyfile.core.context;

import com.openquartz.easyfile.core.entity.ErrorRecord;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 导入数据上下文
 * 包含当前分页的数据和元信息
 *
 * @author svnee
 */
@Data
@Builder
public class ImportDataContext<T> {

    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 导入上下文
     */
    private ImportContext requestContext;

    /**
     * 当前页码 (从1开始)
     */
    private int pageNumber;

    /**
     * 分页大小
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 当前页的起始记录位置 (从0开始)
     */
    private long pageStartPosition;

    /**
     * 当前页的结束记录位置 (从0开始)
     */
    private long pageEndPosition;

    /**
     * 验证错误记录
     */
    private List<ErrorRecord> validationErrors;

    /**
     * 业务处理错误记录
     */
    private List<ErrorRecord> businessErrors;

    /**
     * 是否通过验证
     */
    public boolean isValid() {
        return validationErrors == null || validationErrors.isEmpty();
    }

    /**
     * 添加验证错误
     */
    public void addValidationError(ErrorRecord error) {
        if (validationErrors == null) {
            validationErrors = new java.util.ArrayList<>();
        }
        validationErrors.add(error);
    }

    /**
     * 添加业务错误
     */
    public void addBusinessError(ErrorRecord error) {
        if (businessErrors == null) {
            businessErrors = new java.util.ArrayList<>();
        }
        businessErrors.add(error);
    }

    /**
     * 获取总页数
     */
    public int getTotalPages() {
        return (int) ((totalCount + pageSize - 1) / pageSize);
    }

    /**
     * 是否是最后一页
     */
    public boolean isLastPage() {
        return pageNumber >= getTotalPages();
    }

    /**
     * 是否是第一页
     */
    public boolean isFirstPage() {
        return pageNumber <= 1;
    }
}