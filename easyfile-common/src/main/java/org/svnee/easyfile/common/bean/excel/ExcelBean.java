package org.svnee.easyfile.common.bean.excel;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import lombok.Data;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * ExcelBean
 *
 * @author svnee
 */
@Data
public class ExcelBean implements Closeable {

    /**
     * 切分sheet 最大行数
     * 必须大于 1 (标题行占1行)
     */
    private int segmentationSheetRows = 1000000;

    /**
     * 基础CellStyle
     */
    private CellStyle baseStyle;

    /**
     * 工作簿
     */
    private Workbook workbook;

    /**
     * 当前页
     */
    private Sheet currentSheet;

    /**
     * 总行数
     */
    private int sumRow;

    /**
     * 当前行下标
     */
    private int currentRowIndex;

    /**
     * sheet 下标
     */
    private int sheetIndex;

    /**
     * 关闭流
     */
    @Override
    public void close() {
        if (Objects.nonNull(workbook)) {
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
        }
    }

    public ExcelBean() {
    }

    public ExcelBean(Integer segmentationSheetRows) {
        this.segmentationSheetRows = segmentationSheetRows;
    }

    public Workbook getWorkbook() {
        return Objects.nonNull(workbook) ? workbook : new SXSSFWorkbook(1000);
    }

    /**
     * 获取当前执行的Sheet
     */
    public Sheet getCurrentSheet() {
        if (Objects.isNull(currentSheet) || currentRowIndex >= segmentationSheetRows) {
            return nextSheet();
        }
        return currentSheet;
    }

    /**
     * 获取下一页
     */
    private Sheet nextSheet() {
        sheetIndex++;
        Sheet sheet = workbook.createSheet("SHEET" + sheetIndex);
        currentSheet = sheet;
        currentRowIndex = 0;
        return sheet;
    }

    /**
     * 写入行数
     */
    public void writeRow(int rows) {
        sumRow += rows;
        currentRowIndex += rows;
    }

    /**
     * 克隆style
     */
    public CellStyle cloneStyleByBase() {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.cloneStyleFrom(baseStyle);
        return cellStyle;
    }

    @Override
    public String toString() {
        return "ExcelBean{" +
            "sumRow=" + sumRow +
            ", currentRowIndex=" + currentRowIndex +
            ", sheetIndex=" + sheetIndex +
            '}';
    }
}
