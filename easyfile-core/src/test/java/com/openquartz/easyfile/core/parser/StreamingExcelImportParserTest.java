package com.openquartz.easyfile.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Streaming xlsx + HSSF factory parsers.
 */
public class StreamingExcelImportParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void xlsx_readPage_withoutReopen_returnsPagedRows() throws Exception {
        File excel = createXlsx();
        try (ImportParser<StudentRow> parser = ImportParserFactory.create("xlsx", excel, StudentRow.class)) {
            assertEquals(2L, parser.getTotalCount());
            List<ImportRow<StudentRow>> page1 = parser.readPage(1, 1);
            List<ImportRow<StudentRow>> page2 = parser.readPage(2, 1);
            assertEquals(1, page1.size());
            assertEquals(1L, page1.get(0).getRowNumber());
            assertEquals("Alice", page1.get(0).getData().getName());
            assertEquals(Integer.valueOf(20), page1.get(0).getData().getAge());
            assertNull(page1.get(0).getParseError());
            assertEquals("Bob", page2.get(0).getData().getName());
            assertEquals(0, new BigDecimal("99.5").compareTo(page2.get(0).getData().getScore()));
        }
    }

    @Test
    public void xls_hssf_readPage_mapsAnnotatedFields() throws Exception {
        File excel = createXls();
        try (ImportParser<StudentRow> parser = ImportParserFactory.create("xls", excel, StudentRow.class)) {
            assertEquals(1L, parser.getTotalCount());
            List<ImportRow<StudentRow>> page = parser.readPage(1, 10);
            assertEquals(1, page.size());
            assertEquals(1L, page.get(0).getRowNumber());
            assertEquals("Carol", page.get(0).getData().getName());
            assertEquals(Long.valueOf(2001L), page.get(0).getData().getId());
        }
    }

    private File createXlsx() throws Exception {
        File file = temporaryFolder.newFile("students.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("s1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("age");
            header.createCell(3).setCellValue("score");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1001);
            r1.createCell(1).setCellValue("Alice");
            r1.createCell(2).setCellValue(20);
            r1.createCell(3).setCellValue(88.0);

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1002);
            r2.createCell(1).setCellValue("Bob");
            r2.createCell(2).setCellValue(21);
            r2.createCell(3).setCellValue(99.5);

            workbook.write(out);
        }
        return file;
    }

    private File createXls() throws Exception {
        File file = temporaryFolder.newFile("students.xls");
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("s1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("age");
            header.createCell(3).setCellValue("score");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(2001);
            r1.createCell(1).setCellValue("Carol");
            r1.createCell(2).setCellValue(22);
            r1.createCell(3).setCellValue(77.0);

            workbook.write(out);
        }
        return file;
    }

    @Data
    public static class StudentRow {
        @ExcelImportField(columnIndex = 0)
        private Long id;
        @ExcelImportField(columnIndex = 1)
        private String name;
        @ExcelImportField(columnIndex = 2)
        private Integer age;
        @ExcelImportField(columnIndex = 3)
        private BigDecimal score;
    }
}
