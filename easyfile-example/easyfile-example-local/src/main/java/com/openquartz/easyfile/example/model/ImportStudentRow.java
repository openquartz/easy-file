package com.openquartz.easyfile.example.model;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import lombok.Data;

/**
 * 导入学生行数据模型
 *
 * @author svnee
 */
@Data
public class ImportStudentRow {

    @ExcelImportField(columnIndex = 0, columnHeader = "姓名", required = true, errorMessage = "姓名不能为空")
    private String name;

    @ExcelImportField(columnIndex = 1, columnHeader = "年龄", required = true, errorMessage = "年龄不能为空")
    private Integer age;

    @ExcelImportField(columnIndex = 2, columnHeader = "学校", required = true, errorMessage = "学校不能为空")
    private String school;

    @ExcelImportField(columnIndex = 3, columnHeader = "地址")
    private String address;
}
