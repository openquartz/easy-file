package org.svnee.easyfile.example.model;

import java.util.Date;
import lombok.Data;
import org.svnee.easyfile.common.annotation.ExcelProperty;

/**
 * @author svnee
 **/
@Data
public class Address {

    @ExcelProperty(value = "地址", width = 8 * 512)
    private String addressName;

    @ExcelProperty(value = "过期时间", width = 8 * 1024)
    private Date expireTime;
}
