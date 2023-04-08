package com.openquartz.easyfile.example.model;

import java.util.Date;
import lombok.Data;
import com.openquartz.easyfile.common.annotations.ExcelProperty;

/**
 * @author svnee
 **/
@Data
public class Address {

    @ExcelProperty(value = "地址", width = 8 * 512)
    private String addressName;

    @ExcelProperty(value = "过期时间", width = 8 * 1024)
    private Date expireTime;

    public Address() {
    }

    public Address(String addressName, Date expireTime) {
        this.addressName = addressName;
        this.expireTime = expireTime;
    }
}
