package com.openquartz.easyfile.core.parser;

import java.io.File;

/**
 * Creates {@link ImportParser} instances by normalized file type.
 */
public final class ImportParserFactory {

    private ImportParserFactory() {
    }

    /**
     * @param fileType csv / xlsx / xls (case-insensitive; optional leading dot)
     * @param file     source file
     * @param targetClass row mapping type annotated with {@code @ExcelImportField}
     */
    public static <T> ImportParser<T> create(String fileType, File file, Class<T> targetClass) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass must not be null");
        }
        String normalized = normalize(fileType);
        switch (normalized) {
            case "csv":
                return new CsvImportParser<>(file, targetClass);
            case "xlsx":
                return new StreamingExcelImportParser<>(file, targetClass);
            case "xls":
                return new HssfExcelImportParser<>(file, targetClass);
            default:
                throw new IllegalArgumentException("Unsupported import file type: " + fileType);
        }
    }

    private static String normalize(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new IllegalArgumentException("fileType must not be blank");
        }
        String type = fileType.trim().toLowerCase();
        if (type.startsWith(".")) {
            type = type.substring(1);
        }
        return type;
    }
}
