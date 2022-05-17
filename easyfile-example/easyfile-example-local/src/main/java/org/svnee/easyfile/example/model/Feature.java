package org.svnee.easyfile.example.model;

import lombok.Data;
import org.svnee.easyfile.common.annotation.ExcelProperty;

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
    @ExcelProperty("描述")
    private String desc;

}
