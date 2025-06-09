package com.openquartz.easyfile.example.model;

import lombok.Data;
import com.openquartz.easyfile.common.annotations.ExcelProperty;

/**
 * @author svnee
 **/
@Data
public class Feature {

    @ExcelProperty("${school.feature.id}")
    private Integer id;
    @ExcelProperty("${school.feature.code}")
    private String code;
    @ExcelProperty("${school.feature.name}")
    private String name;
    @ExcelProperty(value = "${school.feature.desc}", width = 8 * 512)
    private String desc;

}
