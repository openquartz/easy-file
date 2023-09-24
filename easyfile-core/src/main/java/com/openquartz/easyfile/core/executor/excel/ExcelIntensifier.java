package com.openquartz.easyfile.core.executor.excel;

import org.apache.poi.ss.usermodel.Workbook;
import com.openquartz.easyfile.common.bean.BaseExporterRequestContext;

/**
 * Excel增强器
 * @author svnee
 */
public interface ExcelIntensifier {

    /**
     * 是否启动
     * @param context context
     * @return 是否启用
     */
    default boolean enable(BaseExporterRequestContext context){
        return true;
    }

    /**
     * 增强Excel workbook
     * @param workbook workbook
     * @param context context
     */
    void enhance(Workbook workbook, BaseExporterRequestContext context);

}
