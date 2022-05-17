package org.svnee.easyfile.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pagination<T> implements Serializable {

    private static final long serialVersionUID = -7445003904869332858L;
    private Long page = 1L;
    private Long pageSize = 0L;
    private Long totalRecords = -1L;
    private List<T> modelList = new ArrayList<>();
    private int extendsSize = 0;
    private Object information;

    public Pagination() {
    }

    public Long getPage() {
        if (this.page == null || this.page.compareTo(1L) < 0) {
            this.page = 1L;
        }

        Long totalPages = this.getTotalPages();
        if (totalPages.compareTo(-1L) > 0 && this.page.compareTo(totalPages) > 0) {
            this.page = totalPages;
        }

        return this.page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getTotalRecords() {
        return this.totalRecords == null ? -1L : this.totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<T> getModelList() {
        return this.modelList;
    }

    public void setModelList(List<T> modelList) {
        this.modelList = modelList;
    }

    public Long getPageSize() {
        return this.pageSize == null ? 0L : this.pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalPages() {
        long totalPages = -1L;
        if (this.getTotalRecords().compareTo(-1L) > 0 && this.getPageSize().compareTo(0L) > 0) {
            totalPages = (long) Math.ceil((double) this.getTotalRecords() / (double) this.getPageSize());
        }

        return totalPages;
    }

    public void initByStartIndex(Long startIndex, Long pageSize) {
        if (startIndex == null) {
            startIndex = 0L;
        }

        this.setPageSize(pageSize);
        if (this.getPageSize().compareTo(0L) > 0) {
            this.setPage((long) Math.floor((double) startIndex / (double) this.getPageSize()) + 1L);
        } else {
            this.setPage(1L);
        }

    }

    public void init(Long page, Long pageSize) {
        this.setPageSize(pageSize);
        this.setPage(page);
    }

    public Long getStartIndex() {
        long startIndex = 0;
        if (this.getPageSize().compareTo(0L) > 0 && this.getPage().compareTo(0L) > 0) {
            startIndex = (this.getPage() - 1L) * this.getPageSize();
        }

        return startIndex;
    }

    public Object getInformation() {
        return this.information;
    }

    public void setInformation(Object information) {
        this.information = information;
    }

    public int getExtendsSize() {
        return this.extendsSize;
    }

    public void setExtendsSize(int extendsSize) {
        this.extendsSize = extendsSize;
    }
}