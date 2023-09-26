package com.openquartz.easyfile.core.executor.excel;

import com.openquartz.easyfile.common.bean.ExportRequestContext;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import com.openquartz.easyfile.common.util.CollectionUtils;

/**
 * Excel增强器
 *
 * @author svnee
 **/
public interface ExcelIntensifierExecutor {

    /**
     * 增强Excel
     *
     * @return excel增强器
     */
    default List<ExcelIntensifier> enhanceExcel() {
        return Collections.emptyList();
    }

    /**
     * 增强Excel
     *
     * @param workbook workbook
     * @param context context
     */
    default void executeEnhance(Workbook workbook, ExportRequestContext context) {
        if (CollectionUtils.isEmpty(enhanceExcel())) {
            return;
        }
        enhanceExcel().stream()
            .filter(e -> e.enable(context))
            .forEach(k -> k.enhance(workbook, context));
    }

}
