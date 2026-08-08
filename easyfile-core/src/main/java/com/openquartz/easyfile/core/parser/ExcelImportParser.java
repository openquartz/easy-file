package com.openquartz.easyfile.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Deprecated compatibility wrapper around {@link ImportParserFactory}.
 * Prefer {@link ImportParserFactory#create(String, File, Class)}.
 *
 * @param <T> row type
 * @author svnee
 * @deprecated use {@link ImportParserFactory} / streaming parsers instead
 */
@Deprecated
@Slf4j
public class ExcelImportParser<T> implements ImportParser<T> {

    private final ImportParser<T> delegate;
    private final String fileType;

    public ExcelImportParser(Class<T> targetClass, File file, String fileType) {
        this.fileType = fileType;
        this.delegate = ImportParserFactory.create(fileType, file, targetClass);
    }

    /**
     * Compatibility constructor. Stream-based construction is no longer supported for
     * multi-page reads; prefer {@link #ExcelImportParser(Class, File, String)}.
     */
    public ExcelImportParser(Class<T> targetClass, InputStream inputStream, String fileType) {
        throw new UnsupportedOperationException(
            "InputStream constructor is deprecated; use File + ImportParserFactory.create(...)");
    }

    @Override
    public long getTotalCount() throws IOException {
        return delegate.getTotalCount();
    }

    @Override
    public List<ImportRow<T>> readPage(long startRow, int pageSize) throws IOException {
        return delegate.readPage(startRow, pageSize);
    }

    /**
     * Exposed for unit tests of numeric conversion.
     */
    protected Object convertValue(Object value, Class<?> targetType) {
        return ImportFieldMapper.convertValue(value, targetType);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String getFileType() {
        return fileType;
    }
}
