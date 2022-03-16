package org.svnee.easyfile.common.bean.excel;

import static org.svnee.easyfile.common.bean.excel.ExcelBean.SEGMENTATION_SHEET_ROWS;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import org.svnee.easyfile.common.annotation.ExcelProperty;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * @author chaofan
 */
@Slf4j
public class ExcelExports {

    public static void writeWorkbook(ExcelBean excelBean, OutputStream outputStream) {
        try {
            excelBean.getWorkbook().write(outputStream);
        } catch (IOException e) {
            log.error("[write excel error] write data to workbook error,excelBean:{}", excelBean, e);
        }
    }

    public static ExcelBean createWorkbook() {
        Workbook workbook = new SXSSFWorkbook(1000);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        ExcelBean excelBean = new ExcelBean();
        excelBean.setWorkbook(workbook);
        excelBean.setBaseStyle(cellStyle);
        return excelBean;
    }

    public static void writeHeader(ExcelBean excelBean, List<Field> exportFieldList) {
        if (Objects.nonNull(exportFieldList) && !exportFieldList.isEmpty() && excelBean.getCurrentRowIndex() == 0) {
            setHeader(excelBean, exportFieldList);
            excelBean.writeRow(1);
        }
    }

    public static <T> void writeData(ExcelBean excelBean, List<Field> exportFieldList, List<T> rowList) {
        if (CollectionUtils.isEmpty(rowList)) {
            return;
        }
        excelBean.getCurrentSheet();
        int rowIndex = excelBean.getCurrentRowIndex();
        int remainSegmentationSheetRows = SEGMENTATION_SHEET_ROWS;
        // 不存在标题行则去掉一行
        if (rowIndex == 0) {
            remainSegmentationSheetRows = remainSegmentationSheetRows - 1;
        }

        if (rowIndex + rowList.size() > remainSegmentationSheetRows) {
            // 针对标题行进行预留一行
            if (excelBean.getCurrentRowIndex() == 0) {
                setRowsWithHeader(excelBean, exportFieldList,
                    rowList.subList(0, remainSegmentationSheetRows - rowIndex - 1));
                writeData(excelBean, exportFieldList,
                    rowList.subList(remainSegmentationSheetRows - rowIndex - 1, rowList.size()));
            } else {
                setRowsWithHeader(excelBean, exportFieldList,
                    rowList.subList(0, remainSegmentationSheetRows - rowIndex));
                writeData(excelBean, exportFieldList,
                    rowList.subList(remainSegmentationSheetRows - rowIndex, rowList.size()));
            }
        } else {
            setRowsWithHeader(excelBean, exportFieldList, rowList);
        }
    }

    private static <T> void setRowsWithHeader(ExcelBean excelBean, List<Field> exportFieldList, List<T> dataRows) {
        writeHeader(excelBean, exportFieldList);
        setRows(excelBean.getCurrentSheet(),
            exportFieldList, dataRows,
            excelBean.getBaseStyle(),
            excelBean.getCurrentRowIndex());
        excelBean.writeRow(dataRows.size());
    }

    private static void setHeader(ExcelBean excelBean, List<Field> exportFields) {
        if (CollectionUtils.isEmpty(exportFields)) {
            return;
        }
        Sheet sheet = excelBean.getCurrentSheet();
        Row headerRow = sheet.createRow(0);
        CellStyle cellStyle = decorateHeader(excelBean);
        int index = 0;
        for (Field field : exportFields) {
            ExcelProperty excelProperty = Objects
                .requireNonNull(AnnotatedElementUtils.findMergedAnnotation(field, ExcelProperty.class));
            sheet.setColumnWidth(index, excelProperty.width());
            String headerName = excelProperty.value();
            Cell cell = headerRow.createCell(index++);
            cell.setCellStyle(cellStyle);
            if (StringUtils.isNotBlank(headerName)) {
                setCellValue(cell, headerName, field);
            } else {
                setCellValue(cell, field, field);
            }
        }
    }

    private static CellStyle decorateHeader(ExcelBean excelBean) {
        CellStyle cellStyle = excelBean.cloneStyleByBase();
        Font font = excelBean.getWorkbook().createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static <T> void setRows(Sheet sheet, List<Field> exportFields, List<T> dataRows, CellStyle cellStyle,
        int startRowNum) {
        int rowIndex = startRowNum;
        for (Object dataRow : dataRows) {
            Row row = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            for (Field field : exportFields) {
                Object value = reflectiveGetFieldValue(dataRow, field);
                Cell cell = row.createCell(columnIndex++);
                cell.setCellStyle(cellStyle);
                setCellValue(cell, value, field);
            }
        }
    }

    private static Object reflectiveGetFieldValue(Object obj, Field filed) {
        Class<?> clazz = obj.getClass();
        try {
            Method getterMethod;
            if (boolean.class.isAssignableFrom((filed.getType()))) {
                getterMethod = ReflectionUtils.findMethod(clazz, booleanGetterMethodName(filed.getName()));
            } else {
                getterMethod = ReflectionUtils.findMethod(clazz, commonGetterMethodName(filed.getName()));
            }
            assert Objects.nonNull(getterMethod);
            return getterMethod.invoke(obj);
        } catch (Exception e) {
            log.error("excel生成异常", e);
            throw new RuntimeException("文件导出异常");
        }
    }

    private static String booleanGetterMethodName(String fieldName) {
        return "is" + firstIndexToUpper(fieldName);
    }

    private static String commonGetterMethodName(String fieldName) {
        return "get" + firstIndexToUpper(fieldName);
    }

    private static String firstIndexToUpper(String str) {
        if (str == null || str.length() <= 0) {
            return "";
        }
        String first = str.charAt(0) + "";
        return str.replaceFirst(first, first.toUpperCase());
    }

    private static void setCellValue(Cell cell, Object obj, Field field) {
        String value = getValue(obj, field);
        // 过滤 null 值转换为 空格
        if (Objects.isNull(value) || "null".equals(value)) {
            cell.setCellValue(StringUtils.EMPTY);
        } else {
            cell.setCellValue(value);
        }
        cell.setCellType(CellType.STRING);
    }

    /**
     * 获取值(针对 null 值处理为 空格)
     *
     * @param obj obj
     * @return {@link String}
     * @date 2019/10/10 15:56
     */
    private static String getValue(Object obj, Field filed) {
        if (Objects.isNull(obj)) {
            return StringUtils.EMPTY;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Date) {
            return formatDate((Date) obj, filed);
        } else {
            return String.valueOf(obj);
        }
    }

    private static String formatDate(Date date, Field field) {
        ExcelProperty excelProperty = AnnotatedElementUtils.findMergedAnnotation(field, ExcelProperty.class);
        assert Objects.nonNull(excelProperty);
        return new SimpleDateFormat(excelProperty.dateFormatter()).format(date);
    }
}
