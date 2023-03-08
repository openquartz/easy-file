package org.svnee.easyfile.core.executor.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;

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
    default boolean enable(BaseDownloaderRequestContext context){
        return true;
    }

    /**
     * 增强Excel workbook
     * @param workbook workbook
     * @param context context
     */
    void enhance(Workbook workbook,BaseDownloaderRequestContext context);

}
