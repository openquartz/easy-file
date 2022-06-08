package org.svnee.easyfile.common.bean.excel;


import java.lang.reflect.Field;
import java.util.List;
import lombok.Data;
import org.svnee.easyfile.common.annotations.ExcelProperty;

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
     * 自定义类型
     */
    private boolean customBean;

    /**
     * 是否包含子字段集合
     */
    private List<ExcelFiled> subFiledList;

    /**
     * 对应的列ID
     * 非父列存在
     */
    private int columnIndex = -1;

    public ExcelFiled(Field field, ExcelProperty excelProperty) {
        this.field = field;
        this.excelProperty = excelProperty;
        this.collection = ReflectionUtils.isCollection(field.getType());
        this.customBean = !ReflectionUtils.isJavaClass(field.getType()) && !collection;
    }

}
