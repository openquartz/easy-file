package com.openquartz.easyfile.example.model;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import lombok.Data;

/**
 * 远程模式导入学生行数据模型
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
}
