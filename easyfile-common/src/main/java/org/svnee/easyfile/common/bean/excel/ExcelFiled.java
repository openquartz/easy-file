package org.svnee.easyfile.common.bean.excel;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import org.svnee.easyfile.common.annotation.ExcelProperty;

/**
 * ExcelField
 *
 * @author svnee
 **/
@Data
public class ExcelFiled {

    /**
     * field
     */
    private Field field;

    /**
     * ExcelProperty
     */
    private ExcelProperty excelProperty;

    /**
     * 是否是集合
     */
    private boolean collection;

    /**
     * 是否包含子字段集合
     */
    private List<ExcelFiled> subFiledList;

    public ExcelFiled(Field field, ExcelProperty excelProperty) {
        this.field = field;
        this.excelProperty = excelProperty;
        this.collection = field.getType().isAssignableFrom(Collection.class);
    }

}
