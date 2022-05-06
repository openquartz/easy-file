package org.svnee.easyfile.common.bean.excel;

/**
 * @author svnee
 **/
public final class ExcelGenProperty {

    private ExcelGenProperty() {
    }

    /**
     * 缓存内存中行数
     */
    private static Integer rowAccessWindowSize = 1000;

    /**
     * 单sheet最大行数
     */
    private static Integer segmentationSheetRows = 1000000;

    public static void setRowAccessWindowSize(Integer rowAccessWindowSize) {
        ExcelGenProperty.rowAccessWindowSize = rowAccessWindowSize;
    }

    public static void setSegmentationSheetRows(Integer segmentationSheetRows) {
        ExcelGenProperty.segmentationSheetRows = segmentationSheetRows;
    }

    public static Integer getRowAccessWindowSize() {
        return rowAccessWindowSize;
    }

    public static Integer getSegmentationSheetRows() {
        return segmentationSheetRows;
    }
}
