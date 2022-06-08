package org.svnee.easyfile.example.model;

import java.util.List;
import org.svnee.easyfile.common.annotations.ExcelProperty;

public class ExcelModel {

    @ExcelProperty(value = "标题", width = 100 * 256)
    private String title;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty(value = "集合")
    private List<String> list;

    public ExcelModel() {
    }

    public ExcelModel(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
