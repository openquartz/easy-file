package com.openquartz.easyfile.core.parser;

import com.openquartz.easyfile.common.annotations.ExcelImportField;
import com.openquartz.easyfile.core.entity.ErrorRecord;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Excel导入解析器
 * 支持Excel 07和Excel 03格式，使用流式读取防止OOM
 *
 * @author svnee
 */
@Slf4j
public class ExcelImportParser<T> implements ImportParser<T> {

    private final Class<T> targetClass;
    private final InputStream inputStream;
    private final String fileType;
    private long totalCount = -1;

    public ExcelImportParser(Class<T> targetClass, InputStream inputStream, String fileType) {
        this.targetClass = targetClass;
        this.inputStream = inputStream;
        this.fileType = fileType;
    }

    @Override
    public long getTotalCount() throws java.io.IOException {
        if (totalCount >= 0) {
            return totalCount;
        }

        try (Workbook workbook = createWorkbook()) {
            Sheet sheet = workbook.getSheetAt(0);
            totalCount = sheet.getLastRowNum(); // 不包括标题行

            return totalCount;
        } catch (Exception e) {
            throw new java.io.IOException("统计行数失败", e);
        }
    }

    @Override
    public List<T> readPage(int pageNum, int pageSize) throws java.io.IOException {
        List<T> result = new ArrayList<>(pageSize);

        try (Workbook workbook = createWorkbook()) {
            Sheet sheet = workbook.getSheetAt(0);

            int startRow = (pageNum - 1) * pageSize + 1; // 跳过标题行
            int endRow = Math.min(startRow + pageSize, sheet.getLastRowNum() + 1);

            for (int i = startRow; i < endRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null && !isRowEmpty(row)) {
                    T obj = parseRowToObject(row, targetClass);
                    if (obj != null) {
                        result.add(obj);
                    }
                }
            }

        } catch (Exception e) {
            throw new java.io.IOException("Excel解析失败", e);
        }

        return result;
    }

    /**
     * 创建工作簿
     * Excel 07使用SXSSFWorkbook实现流式读取
     * Excel 03使用HSSFWorkbook
     */
    protected Workbook createWorkbook() throws java.io.IOException {
        if ("xlsx".equals(fileType)) {
            // 使用SXSSFWorkbook实现流式读取（防OOM）
            return new SXSSFWorkbook(new XSSFWorkbook(inputStream), 1000, true);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }

    /**
     * 解析行数据为对象
     */
    protected T parseRowToObject(Row row, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();

            for (Field field : targetClass.getDeclaredFields()) {
                ExcelImportField annotation = field.getAnnotation(ExcelImportField.class);
                if (annotation != null) {
                    int columnIndex = annotation.columnIndex();
                    if (columnIndex >= 0 && columnIndex <= row.getLastCellNum()) {
                        Cell cell = row.getCell(columnIndex);
                        Object value = getCellValue(cell);

                        if (value != null) {
                            field.setAccessible(true);
                            field.set(instance, convertValue(value, field.getType()));
                        }
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            log.error("行解析失败: row={}, class={}", row.getRowNum(), targetClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 获取单元格值
     */
    protected Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            CellType cellType = cell.getCellType();
            if (cellType == CellType.BLANK || cellType == CellType._NONE) {
                return null;
            }

            switch (cellType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue();
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("获取单元格值失败", e);
            return null;
        }
    }

    /**
     * 转换值为目标类型
     */
    protected Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        String strValue = value.toString().trim();

        if (strValue.isEmpty()) {
            return null;
        }

        try {
            if (targetType.equals(String.class)) {
                return strValue;
            } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(strValue);
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(strValue);
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(strValue);
            } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return Float.parseFloat(strValue);
            } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return Boolean.parseBoolean(strValue);
            } else if (targetType.equals(java.math.BigDecimal.class)) {
                return new java.math.BigDecimal(strValue);
            } else if (targetType.equals(Date.class)) {
                // 处理Excel日期格式
                if (value instanceof Date) {
                    return value;
                }
                return java.text.DateFormat.getInstance().parse(strValue);
            }
        } catch (Exception e) {
            log.warn("类型转换失败: value={}, targetType={}", value, targetType.getSimpleName());
        }

        return value;
    }

    /**
     * 判断行是否为空
     */
    protected boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                CellType cellType = cell.getCellType();
                if (cellType != CellType.BLANK && cellType != CellType._NONE) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Override
    public String getFileType() {
        return fileType;
    }
}
