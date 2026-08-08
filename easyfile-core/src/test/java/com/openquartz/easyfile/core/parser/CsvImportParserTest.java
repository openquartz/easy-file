package com.openquartz.easyfile.core.parser;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * CSV streaming import parser.
 */
public class CsvImportParserTest {

    @Test
    public void csv_readPage_returnsRowNumbers() throws Exception {
        File f = File.createTempFile("imp", ".csv");
        f.deleteOnExit();
        Files.write(f.toPath(), Arrays.asList("name,age", "a,1", "b,2"));
        try (ImportParser<DemoRow> parser = ImportParserFactory.create("csv", f, DemoRow.class)) {
            Assert.assertEquals(2L, parser.getTotalCount());
            List<ImportRow<DemoRow>> page = parser.readPage(1, 10);
            Assert.assertEquals(1L, page.get(0).getRowNumber());
            Assert.assertEquals("a", page.get(0).getData().getName());
        }
    }

    @Data
    public static class DemoRow {
        @ExcelImportField(columnIndex = 0)
        private String name;
        @ExcelImportField(columnIndex = 1)
        private Integer age;
    }
}
