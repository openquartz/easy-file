package com.openquartz.easyfile.example.model;

import lombok.Data;
import com.openquartz.easyfile.common.annotations.ExcelProperty;

/**
 * @author svnee
 **/
@Data
public class Feature {

    @ExcelProperty("ID")
    private Integer id;
    @ExcelProperty("编码")
    private String code;
    @ExcelProperty("名字")
    private String name;
    @ExcelProperty(value = "描述", width = 8 * 512)
    private String desc;

}
