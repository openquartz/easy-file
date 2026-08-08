package com.openquartz.easyfile.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Excel import parser: numeric conversion + multi-page re-read from File.
 */
public class ExcelImportParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldConvertNumericCellToIntegerAndLong() throws Exception {
        File excel = createSampleExcel();
        ExcelImportParser<StudentRow> parser = new ExcelImportParser<>(StudentRow.class, excel, "xlsx");
        List<StudentRow> page = parser.readPage(1, 10);
        assertEquals(2, page.size());
        assertEquals(Integer.valueOf(20), page.get(0).getAge());
        assertEquals(Long.valueOf(1001L), page.get(0).getId());
        assertEquals("Alice", page.get(0).getName());
        assertEquals(0, new BigDecimal("99.5").compareTo(page.get(1).getScore()));
        parser.close();
    }

    @Test
    public void shouldAllowMultipleWorkbookOpensFromSameFile() throws Exception {
        File excel = createSampleExcel();
        ExcelImportParser<StudentRow> parser = new ExcelImportParser<>(StudentRow.class, excel, "xlsx");
        long total = parser.getTotalCount();
        assertEquals(2L, total);
        List<StudentRow> page1 = parser.readPage(1, 1);
        List<StudentRow> page2 = parser.readPage(2, 1);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertEquals("Alice", page1.get(0).getName());
        assertEquals("Bob", page2.get(0).getName());
        parser.close();
    }

    @Test
    public void convertValueShouldHandleDoubleToInteger() {
        ExcelImportParser<StudentRow> parser = new ExcelImportParser<>(StudentRow.class, new File("noop"), "xlsx");
        Object converted = parser.convertValue(20.0d, Integer.class);
        assertNotNull(converted);
        assertTrue(converted instanceof Integer);
        assertEquals(20, converted);
        assertEquals(30L, parser.convertValue(30.0d, Long.class));
        assertEquals(1.5d, parser.convertValue(1.5d, Double.class));
    }

    private File createSampleExcel() throws Exception {
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
