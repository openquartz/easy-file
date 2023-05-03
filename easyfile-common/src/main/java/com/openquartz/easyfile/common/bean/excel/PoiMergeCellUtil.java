package com.openquartz.easyfile.common.bean.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 纵向合并单元格工具类
 *
 * @author svnee
 */
public final class PoiMergeCellUtil {

    private PoiMergeCellUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiMergeCellUtil.class);

    /**
     * 从 fistRow 到 lastRow
     * 从 firstCol 到 lastCol
     *
     * @param sheet sheet
     * @param firstRow firstRow
     * @param lastRow lastRow
     * @param firstCol firstCol
     * @param lastCol lastCol
     */
    public static void addMergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        try {
            sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        } catch (Exception e) {
            LOGGER.debug("发生了一次合并单元格错误,{},{},{},{}", firstRow, lastRow, firstCol, lastCol);
            // 忽略掉合并的错误,不打印异常
            LOGGER.debug(e.getMessage(), e);
        }
    }

}
