package com.openquartz.easyfile.common.util.page;

import lombok.Data;

/**
 * 页
 */
@Data
public class Page {

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 总数
     */
    private Integer total;

    public Page(Integer pageNum, Integer pageSize, Integer total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }
}
