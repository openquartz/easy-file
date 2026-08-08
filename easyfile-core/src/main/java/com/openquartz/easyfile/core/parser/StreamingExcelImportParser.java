package com.openquartz.easyfile.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Streaming xlsx import parser using POI XSSF SAX/event model.
 * Sheet content is parsed once; pages are served from the in-memory row buffer
 * without reopening the workbook per page.
 *
 * @param <T> row type
 */
@Slf4j
public class StreamingExcelImportParser<T> implements ImportParser<T> {

    private final File file;
    private final Class<T> targetClass;
    private List<List<String>> dataRows;
    private OPCPackage opcPackage;

    public StreamingExcelImportParser(File file, Class<T> targetClass) {
        this.file = file;
        this.targetClass = targetClass;
    }

    @Override
    public long getTotalCount() throws IOException {
        ensureLoaded();
        return dataRows.size();
    }

    @Override
    public List<ImportRow<T>> readPage(long startRow, int pageSize) throws IOException {
        if (startRow < 1 || pageSize <= 0) {
            return Collections.emptyList();
        }
        ensureLoaded();
        int fromIndex = (int) (startRow - 1);
        if (fromIndex >= dataRows.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, dataRows.size());
        List<ImportRow<T>> page = new ArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            long rowNumber = i + 1L;
            page.add(ImportFieldMapper.mapRow(targetClass, rowNumber, dataRows.get(i)));
        }
        return page;
    }

    private void ensureLoaded() throws IOException {
        if (dataRows != null) {
            return;
        }
        try {
            opcPackage = OPCPackage.open(file);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            if (!sheets.hasNext()) {
                dataRows = Collections.emptyList();
                return;
            }
            try (InputStream sheetStream = sheets.next()) {
                dataRows = parseSheet(styles, strings, sheetStream);
            }
        } catch (OpenXML4JException | SAXException | ParserConfigurationException e) {
            throw new IOException("xlsx 流式解析失败", e);
        }
    }

    private List<List<String>> parseSheet(StylesTable styles,
                                          ReadOnlySharedStringsTable strings,
                                          InputStream sheetStream)
        throws IOException, SAXException, ParserConfigurationException {
        RowCollectingHandler handler = new RowCollectingHandler();
        ContentHandler contentHandler = new XSSFSheetXMLHandler(styles, strings, handler, false);
        XMLReader parser = XMLHelper.newXMLReader();
        parser.setContentHandler(contentHandler);
        parser.parse(new InputSource(sheetStream));
        return handler.getDataRows();
    }

    @Override
    public void close() throws IOException {
        dataRows = null;
        if (opcPackage != null) {
            try {
                opcPackage.close();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("关闭 xlsx 失败", e);
            } finally {
                opcPackage = null;
            }
        }
    }

    @Override
    public String getFileType() {
        return "xlsx";
    }

    private static final class RowCollectingHandler implements SheetContentsHandler {

        private final List<List<String>> dataRows = new ArrayList<>();
        private final Map<Integer, String> currentCells = new HashMap<>();
        private boolean headerSkipped;

        @Override
        public void startRow(int rowNum) {
            currentCells.clear();
        }

        @Override
        public void endRow(int rowNum) {
            if (!headerSkipped) {
                headerSkipped = true;
                currentCells.clear();
                return;
            }
            if (currentCells.isEmpty()) {
                return;
            }
            int maxIndex = currentCells.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
            if (maxIndex < 0) {
                return;
            }
            List<String> values = new ArrayList<>(maxIndex + 1);
            boolean anyNonEmpty = false;
            for (int i = 0; i <= maxIndex; i++) {
                String v = currentCells.get(i);
                if (v != null && !v.trim().isEmpty()) {
                    anyNonEmpty = true;
                }
                values.add(v == null ? "" : v);
            }
            if (anyNonEmpty) {
                dataRows.add(values);
            }
            currentCells.clear();
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if (cellReference == null) {
                return;
            }
            int col = new CellReference(cellReference).getCol();
            currentCells.put(col, formattedValue == null ? "" : formattedValue);
        }

        List<List<String>> getDataRows() {
            return dataRows;
        }
    }
}
