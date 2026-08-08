package com.openquartz.easyfile.core.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CSV streaming import parser. Reads sequentially; does not reload the file per page
 * when pages are consumed in ascending {@code startRow} order.
 *
 * @param <T> row type
 */
public class CsvImportParser<T> implements ImportParser<T> {

    private final File file;
    private final Class<T> targetClass;
    private BufferedReader reader;
    private long nextRowNumber = 1L;
    private Long totalCount;
    private boolean headerSkipped;

    public CsvImportParser(File file, Class<T> targetClass) {
        this.file = file;
        this.targetClass = targetClass;
    }

    @Override
    public long getTotalCount() throws IOException {
        if (totalCount != null) {
            return totalCount;
        }
        long count = 0L;
        try (BufferedReader counting = openReader()) {
            boolean first = true;
            String line;
            while ((line = counting.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    count++;
                }
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
        ensureReader();
        if (startRow < nextRowNumber) {
            resetReader();
        }
        while (nextRowNumber < startRow) {
            String skipped = readNextDataLine();
            if (skipped == null) {
                return Collections.emptyList();
            }
            nextRowNumber++;
        }
        List<ImportRow<T>> page = new ArrayList<>(pageSize);
        for (int i = 0; i < pageSize; i++) {
            String line = readNextDataLine();
            if (line == null) {
                break;
            }
            List<String> rawValues = splitCsvLine(line);
            page.add(ImportFieldMapper.mapRow(targetClass, nextRowNumber, rawValues));
            nextRowNumber++;
        }
        return page;
    }

    private void ensureReader() throws IOException {
        if (reader == null) {
            resetReader();
        }
    }

    private void resetReader() throws IOException {
        closeReaderQuietly();
        reader = openReader();
        headerSkipped = false;
        nextRowNumber = 1L;
        skipHeaderIfNeeded();
    }

    private BufferedReader openReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    private void skipHeaderIfNeeded() throws IOException {
        if (!headerSkipped && reader != null) {
            reader.readLine();
            headerSkipped = true;
        }
    }

    private String readNextDataLine() throws IOException {
        ensureReader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }

    static List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    private void closeReaderQuietly() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
                // ignore
            }
            reader = null;
        }
    }

    @Override
    public void close() throws IOException {
        closeReaderQuietly();
    }

    @Override
    public String getFileType() {
        return "csv";
    }
}
