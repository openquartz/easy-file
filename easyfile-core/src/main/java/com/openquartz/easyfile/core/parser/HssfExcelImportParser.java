package com.openquartz.easyfile.core.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * xls (HSSF) import parser. Opens the workbook once and pages from the open sheet.
 * Intended for smaller classic Excel files.
 *
 * @param <T> row type
 */
public class HssfExcelImportParser<T> implements ImportParser<T> {

    private final File file;
    private final Class<T> targetClass;
    private HSSFWorkbook workbook;
    private Sheet sheet;
    private final DataFormatter formatter = new DataFormatter();
    private Long totalCount;

    public HssfExcelImportParser(File file, Class<T> targetClass) {
        this.file = file;
        this.targetClass = targetClass;
    }

    @Override
    public long getTotalCount() throws IOException {
        ensureOpen();
        if (totalCount != null) {
            return totalCount;
        }
        long count = 0L;
        int last = sheet.getLastRowNum();
        for (int i = 1; i <= last; i++) {
            Row row = sheet.getRow(i);
            if (row != null && !isRowEmpty(row)) {
                count++;
            }
        }
        totalCount = count;
        return totalCount;
    }

    @Override
    public List<ImportRow<T>> readPage(long startRow, int pageSize) throws IOException {
        if (startRow < 1 || pageSize <= 0) {
            return Collections.emptyList();
        }
        ensureOpen();
        List<ImportRow<T>> page = new ArrayList<>(pageSize);
        long dataRowOrdinal = 0L;
        int last = sheet.getLastRowNum();
        for (int i = 1; i <= last && page.size() < pageSize; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            dataRowOrdinal++;
            if (dataRowOrdinal < startRow) {
                continue;
            }
            List<String> rawValues = toRawValues(row);
            page.add(ImportFieldMapper.mapRow(targetClass, dataRowOrdinal, rawValues));
        }
        return page;
    }

    private void ensureOpen() throws IOException {
        if (workbook != null) {
            return;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            workbook = new HSSFWorkbook(in);
            sheet = workbook.getSheetAt(0);
        }
    }

    private List<String> toRawValues(Row row) {
        short lastCell = row.getLastCellNum();
        if (lastCell < 0) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>(lastCell);
        for (int c = 0; c < lastCell; c++) {
            Cell cell = row.getCell(c);
            values.add(cell == null ? "" : formatter.formatCellValue(cell));
        }
        return values;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                CellType cellType = cell.getCellType();
                if (cellType != CellType.BLANK && cellType != CellType._NONE) {
                    String formatted = formatter.formatCellValue(cell);
                    if (formatted != null && !formatted.trim().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
            workbook = null;
            sheet = null;
        }
    }

    @Override
    public String getFileType() {
        return "xls";
    }
}
