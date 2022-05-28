package org.svnee.easyfile.common.bean.excel;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Sheet Bean Info
 *
 * @author svnee
 **/
public class SheetBean {

    /**
     * 当前页
     */
    private Sheet currentSheet;

    /**
     * 总行数
     */
    private int sumRow = 0;

    /**
     * 当前行下标
     */
    private int currentRowIndex = 0;

    /**
     * sheet 下标
     */
    private int sheetIndex = 0;

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void nextSheet(Sheet nextSheet) {
        currentSheet = nextSheet;
        currentRowIndex = 0;
        sheetIndex++;
    }

    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public int getSumRow() {
        return sumRow;
    }

    public int getCurrentRowIndex() {
        return currentRowIndex;
    }

    public void writeRows(int rows) {
        sumRow += rows;
        currentRowIndex += rows;
    }

    @Override
    public String toString() {
        return "SheetBean{" +
            "currentSheet=" + currentSheet +
            ", sumRow=" + sumRow +
            ", currentRowIndex=" + currentRowIndex +
            ", sheetIndex=" + sheetIndex +
            '}';
    }
}
