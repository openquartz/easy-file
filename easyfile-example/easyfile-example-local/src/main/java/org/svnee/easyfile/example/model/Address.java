package org.svnee.easyfile.example.model;

import java.util.Date;
import lombok.Data;
import org.svnee.easyfile.common.annotation.ExcelProperty;

/**
 * @author svnee
 **/
@Data
public class Address {

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty(value = "过期时间")
    private Date expireTime;
}
