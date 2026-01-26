package com.openquartz.easyfile.example.importer.dto;

import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.common.bean.ImportErrorMsgAware;
import lombok.Data;

@Data
public class StudentImportDTO implements ImportErrorMsgAware {
    @ExcelProperty("Name")
    private String name;

    @ExcelProperty("Age")
    private Integer age;

    @ExcelProperty("Address")
    private String address;

    @ExcelProperty("Error Message")
    private String errorMsg;
}
