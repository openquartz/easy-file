package com.openquartz.easyfile.common.bean.excel;

import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.ReflectionUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

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
            outputStream.flush();
        } catch (IOException e) {
            log.error("[ExcelExports#writeWorkbook] write data to workbook error,excelBean:{}", excelBean, e);
        }
    }

    /**
     * 写出空workbook
     *
     * @param outputStream 输出流
     */
    public static void writeEmptyWorkbook(OutputStream outputStream) {
        try {
            ExcelBean excelBean = createWorkbook();
            excelBean.getWorkbook().write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.error("[ExcelExports#writeEmptyWorkbook] write data to workbook error", e);
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
    public static int writeHeader(ExcelBean excelBean, List<ExcelFiled> exportFieldList, String sheetGroup) {
        if (CollectionUtils.isNotEmpty(exportFieldList) && excelBean.getCurrentRowIndex(sheetGroup) == 0) {
            return setHeader(excelBean, exportFieldList, sheetGroup);
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
    public static <T> void writeData(ExcelBean excelBean, List<ExcelFiled> exportFieldList, List<T> rowList,
        String sheetGroup) {
        if (CollectionUtils.isEmpty(rowList)) {
            return;
        }
        excelBean.getCurrentSheet(sheetGroup);
        writeRows(excelBean, exportFieldList, rowList, sheetGroup);
    }

    /**
     * 设置表头
     *
     * @param excelBean excelBean
     * @param exportFields exportFields
     * @return 表头占用的行数
     */
    private static int setHeader(ExcelBean excelBean, List<ExcelFiled> exportFields, String sheetGroup) {
        int titleRow = 0;
        if (CollectionUtils.isEmpty(exportFields)) {
            return titleRow;
        }
        Sheet sheet = excelBean.getCurrentSheet(sheetGroup);

        Row headerRow = sheet.createRow(0);
        titleRow += 1;

        boolean needSubTitle = exportFields.stream()
            .anyMatch(e -> CollectionUtils.isNotEmpty(e.getSubFiledList())
                && StringUtils.isNotBlank(e.getExcelProperty().value()));
        Row subHeaderRow = needSubTitle ? sheet.createRow(1) : headerRow;
        titleRow = needSubTitle ? titleRow + 1 : titleRow;
        excelBean.writeRow(titleRow, sheetGroup);

        // 装饰表头标题格式
        CellStyle cellStyle = excelBean.decorateHeader();
        for (ExcelFiled field : exportFields) {
            ExcelProperty excelProperty = field.getExcelProperty();
            sheet.setColumnWidth(field.getFrmColumnIndex(), excelProperty.width());
            if (!field.isCollection() && CollectionUtils.isEmpty(field.getSubFiledList())) {
                Cell cell = headerRow.createCell(field.getFrmColumnIndex());
                cell.setCellStyle(cellStyle);
                if (StringUtils.isNotBlank(excelProperty.value())) {
                    setCellValue(cell, excelProperty.value(), field);
                } else {
                    setCellValue(cell, field.getField().getName(), field);
                }

                if (needSubTitle) {
                    Cell subCell = subHeaderRow.createCell(field.getFrmColumnIndex());
                    subCell.setCellStyle(cellStyle);
                    // 合并单元格到列
                    // 设置单元格并做合并 (index-->index+subIndex)
                    PoiMergeCellUtil.addMergedRegion(sheet, 0, 1, field.getFrmColumnIndex(), field.getFrmColumnIndex());
                }
            } else {
                // 如果设置的单元格属性的为空则不设置子表头
                if (StringUtils.isNotBlank(field.getExcelProperty().value())) {
                    Cell cell = headerRow.createCell(field.getFrmColumnIndex());
                    cell.setCellStyle(cellStyle);
                    setCellValue(cell, field.getExcelProperty().value(), field);
                    // 设置单元格并做合并 (index-->index+subIndex)
                    PoiMergeCellUtil.addMergedRegion(sheet, 0, 0, field.getFrmColumnIndex(), field.getToColumnIndex());
                }
                for (ExcelFiled subField : field.getSubFiledList()) {
                    Row customTitleRow =
                        StringUtils.isBlank(field.getExcelProperty().value()) ? headerRow : subHeaderRow;
                    sheet.setColumnWidth(subField.getFrmColumnIndex(), subField.getExcelProperty().width());
                    Cell cell = customTitleRow.createCell(subField.getFrmColumnIndex());
                    cell.setCellStyle(cellStyle);
                    if (StringUtils.isNotBlank(subField.getExcelProperty().value())) {
                        setCellValue(cell, subField.getExcelProperty().value(), subField);
                    } else {
                        setCellValue(cell, subField.getField().getName(), subField);
                    }
                    if (StringUtils.isBlank(field.getExcelProperty().value()) && needSubTitle) {
                        // 设置单元格并做合并 (0,index-->1,index)
                        PoiMergeCellUtil
                            .addMergedRegion(sheet, 0, 1, subField.getFrmColumnIndex(), subField.getToColumnIndex());
                    }
                }
            }
        }
        //冻结标题行
        sheet.createFreezePane(0, titleRow);
        return titleRow;
    }

    /**
     * 写入数据
     *
     * @param excelBean excelBean
     * @param exportFields exportFields
     * @param dataRows dataRows
     * @param <T> T
     */
    private static <T> void writeRows(ExcelBean excelBean, List<ExcelFiled> exportFields, List<T> dataRows,
        String sheetGroup) {
        CellStyle cellStyle = excelBean.getBaseStyle();
        for (Object dataRow : dataRows) {
            writeHeader(excelBean, exportFields, sheetGroup);
            int rowIndex = excelBean.getCurrentRowIndex(sheetGroup);

            Sheet sheet = excelBean.getCurrentSheet(sheetGroup);
            Row row = sheet.createRow(rowIndex);
            int maxCurrentSubRowIndex = rowIndex;
            for (ExcelFiled field : exportFields) {
                // 基本类型数据
                if (!field.isCollection()
                    && ReflectionUtils.isJavaClass(field.getField().getType())) {
                    Object value = ReflectionUtils.reflectiveGetFieldValue(dataRow, field.getField());

                    Cell cell = row.createCell(field.getFrmColumnIndex());
                    cell.setCellStyle(cellStyle);
                    setCellValue(cell, value, field);
                } else if (field.isCustomBean()) {
                    // 用户自定义类型
                    Object value = ReflectionUtils.reflectiveGetFieldValue(dataRow, field.getField());
                    setSubCell(cellStyle, row, field, value);
                } else {
                    // 集合类型
                    Object value = ReflectionUtils.reflectiveGetFieldValue(dataRow, field.getField());
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
                                setSubCell(cellStyle, subRow, field, subRowData);
                            }
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
                                .addMergedRegion(sheet, rowIndex, maxCurrentSubRowIndex,
                                    exportField.getFrmColumnIndex(),
                                    exportField.getFrmColumnIndex());
                        } else {
                            for (ExcelFiled subExcelFiled : exportField.getSubFiledList()) {
                                PoiMergeCellUtil
                                    .addMergedRegion(sheet, rowIndex, maxCurrentSubRowIndex,
                                        subExcelFiled.getFrmColumnIndex(),
                                        subExcelFiled.getFrmColumnIndex());
                            }
                        }
                    }
                }
            }

            // 如果此时已经大于当前sheet页则重新创建sheet
            excelBean.writeRow(maxCurrentSubRowIndex - rowIndex + 1, sheetGroup);
            excelBean.getCurrentSheet(sheetGroup);
        }
    }

    /**
     * 设置当前子列单元格的值
     *
     * @param cellStyle 单元格式
     * @param row 行
     * @param field 字段
     * @param value 值
     * @return 当前下表
     */
    private static void setSubCell(CellStyle cellStyle, Row row, ExcelFiled field, Object value) {

        for (ExcelFiled subField : field.getSubFiledList()) {
            Object subData =
                Objects.nonNull(value) ? ReflectionUtils.reflectiveGetFieldValue(value, subField.getField()) : null;
            Cell cell = row.createCell(subField.getFrmColumnIndex());
            cell.setCellStyle(cellStyle);
            setCellValue(cell, subData, subField);
        }
    }

    private static void setCellValue(Cell cell, Object obj, ExcelFiled field) {
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
     */
    private static String getValue(Object obj, ExcelFiled filed) {
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

    private static String formatDate(Date date, ExcelFiled field) {
        ExcelProperty excelProperty = field.getExcelProperty();
        assert Objects.nonNull(excelProperty);
        return new SimpleDateFormat(excelProperty.dateFormatter()).format(date);
    }
}
