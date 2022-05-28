package org.svnee.easyfile.common.bean.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * 导出下载工具类-支持
 *
 * @author svnee
 */
@Slf4j
public final class ExcelExports {

    private ExcelExports() {
    }

    /**
     * 写入数据到Excel-流
     *
     * @param excelBean excel类
     * @param outputStream 输出流
     */
    public static void writeWorkbook(ExcelBean excelBean, OutputStream outputStream) {
        try {
            excelBean.getWorkbook().write(outputStream);
        } catch (IOException e) {
            log.error("[ExcelExports#writeWorkbook] write data to workbook error,excelBean:{}", excelBean, e);
        }
    }

    public static ExcelBean createWorkbook() {
        Workbook workbook = new SXSSFWorkbook(ExcelGenProperty.getRowAccessWindowSize());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        ExcelBean excelBean = new ExcelBean(ExcelGenProperty.getSegmentationSheetRows());
        excelBean.setWorkbook(workbook);
        excelBean.setBaseStyle(cellStyle);
        return excelBean;
    }

    /**
     * 写入表头
     *
     * @param excelBean excelBean
     * @param exportFieldList 导出字段
     * @return 占用行数
     */
    public static int writeHeader(ExcelBean excelBean, List<ExcelFiled> exportFieldList) {
        if (CollectionUtils.isNotEmpty(exportFieldList) && excelBean.getCurrentRowIndex() == 0) {
            return setHeader(excelBean, exportFieldList);
        }
        return 0;
    }

    /**
     * 写入数据
     *
     * @param excelBean bean
     * @param exportFieldList exportFiledList
     * @param rowList rowList
     * @param <T> T
     */
    public static <T> void writeData(ExcelBean excelBean, List<ExcelFiled> exportFieldList, List<T> rowList) {
        if (CollectionUtils.isEmpty(rowList)) {
            return;
        }
        excelBean.getCurrentSheet();
        writeRows(excelBean, exportFieldList, rowList);
    }

    /**
     * 设置表头
     *
     * @param excelBean excelBean
     * @param exportFields exportFields
     * @return 表头占用的行数
     */
    private static int setHeader(ExcelBean excelBean, List<ExcelFiled> exportFields) {
        int titleRow = 0;
        if (CollectionUtils.isEmpty(exportFields)) {
            return titleRow;
        }
        Sheet sheet = excelBean.getCurrentSheet();

        Row headerRow = sheet.createRow(0);
        titleRow += 1;

        boolean needSubTitle = exportFields.stream()
            .anyMatch(e -> CollectionUtils.isNotEmpty(e.getSubFiledList())
                && StringUtils.isNotBlank(e.getExcelProperty().value()));
        Row subHeaderRow = needSubTitle ? sheet.createRow(1) : headerRow;
        titleRow = needSubTitle ? titleRow + 1 : titleRow;
        excelBean.writeRow(titleRow);

        CellStyle cellStyle = decorateHeader(excelBean);
        int index = 0;
        for (ExcelFiled field : exportFields) {
            ExcelProperty excelProperty = field.getExcelProperty();
            sheet.setColumnWidth(index, excelProperty.width());
            if (!field.isCollection() && CollectionUtils.isEmpty(field.getSubFiledList())) {
                Cell cell = headerRow.createCell(index);
                cell.setCellStyle(cellStyle);
                if (StringUtils.isNotBlank(excelProperty.value())) {
                    setCellValue(cell, excelProperty.value(), field);
                } else {
                    setCellValue(cell, field.getField().getName(), field);
                }

                if (needSubTitle) {
                    Cell subCell = subHeaderRow.createCell(index);
                    subCell.setCellStyle(cellStyle);
                    // 合并单元格到列
                    // 设置单元格并做合并 (index-->index+subIndex)
                    PoiMergeCellUtil.addMergedRegion(sheet, 0, 1, index, index);
                }
                index++;
            } else {
                // 如果设置的单元格属性的为空则不设置子表头
                if (StringUtils.isNotBlank(field.getExcelProperty().value())) {
                    Cell cell = headerRow.createCell(index);
                    cell.setCellStyle(cellStyle);
                    setCellValue(cell, field.getExcelProperty().value(), field);
                    // 设置单元格并做合并 (index-->index+subIndex)
                    PoiMergeCellUtil.addMergedRegion(sheet, 0, 0, index, field.getSubFiledList().size() - 1 + index);
                }
                for (ExcelFiled subField : field.getSubFiledList()) {
                    Row customTitleRow =
                        StringUtils.isBlank(field.getExcelProperty().value()) ? headerRow : subHeaderRow;
                    sheet.setColumnWidth(index, subField.getExcelProperty().width());
                    Cell cell = customTitleRow.createCell(index);
                    cell.setCellStyle(cellStyle);
                    if (StringUtils.isNotBlank(subField.getExcelProperty().value())) {
                        setCellValue(cell, subField.getExcelProperty().value(), subField);
                    } else {
                        setCellValue(cell, subField.getField().getName(), subField);
                    }
                    if (StringUtils.isBlank(field.getExcelProperty().value()) && needSubTitle) {
                        // 设置单元格并做合并 (0,index-->1,index)
                        PoiMergeCellUtil.addMergedRegion(sheet, 0, 1, index, index);
                    }
                    index++;
                }
            }
        }
        return titleRow;
    }

    private static CellStyle decorateHeader(ExcelBean excelBean) {
        CellStyle cellStyle = excelBean.cloneStyleByBase();
        Font font = excelBean.getWorkbook().createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 写入数据
     *
     * @param excelBean excelBean
     * @param exportFields exportFields
     * @param dataRows dataRows
     * @param <T> T
     */
    private static <T> void writeRows(ExcelBean excelBean, List<ExcelFiled> exportFields, List<T> dataRows) {
        CellStyle cellStyle = excelBean.getBaseStyle();
        for (Object dataRow : dataRows) {
            writeHeader(excelBean, exportFields);
            int rowIndex = excelBean.getCurrentRowIndex();

            Sheet sheet = excelBean.getCurrentSheet();
            Row row = sheet.createRow(rowIndex);
            int columnIndex = 0;
            int maxCurrentSubRowIndex = rowIndex;
            for (ExcelFiled field : exportFields) {
                if (!field.isCollection()
                    && org.svnee.easyfile.common.bean.excel.ReflectionUtils.isJavaClass(field.getField().getType())) {
                    Object value = reflectiveGetFieldValue(dataRow, field.getField());

                    field.setColumnIndex(columnIndex);

                    Cell cell = row.createCell(columnIndex++);
                    cell.setCellStyle(cellStyle);
                    setCellValue(cell, value, field);
                } else if (field.isCustomBean()) {
                    // 用户自定义类型
                    Object value = reflectiveGetFieldValue(dataRow, field.getField());
                    columnIndex = setSubCell(cellStyle, row, columnIndex, field, value);
                } else {
                    // 集合类型
                    Object value = reflectiveGetFieldValue(dataRow, field.getField());
                    if (value != null) {
                        Collection<?> subDataCollection = (Collection<?>) value;
                        if (CollectionUtils.isNotEmpty(subDataCollection)) {
                            Row subRow = row;
                            int currentSubRowIndex = rowIndex;
                            for (Object subRowData : subDataCollection) {
                                if (currentSubRowIndex > rowIndex) {
                                    subRow = sheet.createRow(currentSubRowIndex);
                                }

                                currentSubRowIndex++;
                                setSubCell(cellStyle, subRow, columnIndex, field, subRowData);
                            }
                            columnIndex += field.getSubFiledList().size();
                            // 取最大的下标,第一行共用父列的第一行
                            maxCurrentSubRowIndex = Math.max(currentSubRowIndex - 1, maxCurrentSubRowIndex);
                        }
                    }
                }
            }

            // 当前存在子行列时需要做合并单元格操作
            if (maxCurrentSubRowIndex > rowIndex) {
                for (ExcelFiled exportField : exportFields) {
                    if (!exportField.isCollection()) {
                        if (!exportField.isCustomBean()) {
                            PoiMergeCellUtil
                                .addMergedRegion(sheet, rowIndex, maxCurrentSubRowIndex, exportField.getColumnIndex(),
                                    exportField.getColumnIndex());
                        } else {
                            for (ExcelFiled subExcelFiled : exportField.getSubFiledList()) {
                                PoiMergeCellUtil
                                    .addMergedRegion(sheet, rowIndex, maxCurrentSubRowIndex,
                                        subExcelFiled.getColumnIndex(),
                                        subExcelFiled.getColumnIndex());
                            }
                        }
                    }
                }
            }

            // 如果此时已经大于当前sheet页则重新创建sheet
            excelBean.writeRow(maxCurrentSubRowIndex - rowIndex + 1);
            excelBean.getCurrentSheet();
        }
    }

    /**
     * 设置当前子列单元格的值
     *
     * @param cellStyle 单元格式
     * @param row 行
     * @param columnIndex 列
     * @param field 字段
     * @param value 值
     * @return 当前下表
     */
    private static int setSubCell(CellStyle cellStyle, Row row, int columnIndex, ExcelFiled field, Object value) {

        if (Objects.isNull(value)) {
            return columnIndex;
        }

        for (ExcelFiled subField : field.getSubFiledList()) {

            subField.setColumnIndex(columnIndex);

            Object subData = reflectiveGetFieldValue(value, subField.getField());
            Cell cell = row.createCell(columnIndex++);
            cell.setCellStyle(cellStyle);
            setCellValue(cell, subData, subField);
        }
        return columnIndex;
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
            assert getterMethod != null;
            return getterMethod.invoke(obj);
        } catch (Exception e) {
            log.error("ExcelExports#reflectiveGetFieldValue,excel生成异常", e);
            throw new EasyFileException(CommonErrorCode.DOWNLOAD_EXECUTE_REFLECT_ERROR);
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
            return StringUtils.EMPTY;
        }
        String first = str.charAt(0) + StringUtils.EMPTY;
        return str.replaceFirst(first, first.toUpperCase());
    }

    private static void setCellValue(Cell cell, Object obj, ExcelFiled field) {
        String value = getValue(obj, field.getField());
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
