package com.openquartz.easyfile.core;

import com.openquartz.easyfile.core.entity.ErrorRecord;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.storage.file.UploadService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * 导入结果文件生成器
 * 生成包含统计信息和错误记录的Excel文件
 *
 * @author svnee
 */
@RequiredArgsConstructor
public class ImportResultFileGenerator {

    private static final String TEMP_DIR = "/tmp/easyfile/import_result/";
    private final UploadService uploadService;

    /**
     * 生成结果文件
     *
     * @param appId 应用ID
     * @param registerId 注册ID
     * @param errorRecords 错误记录列表
     * @param totalCount 总记录数
     * @param successCount 成功记录数
     * @param failCount 失败记录数
     * @return 结果文件URL
     */
    public String generate(String appId, Long registerId, List<ErrorRecord> errorRecords,
                          long totalCount, long successCount, long failCount) {
        try {
            // 创建临时结果文件
            File resultFile = createTempResultFile(registerId);

            // 创建Excel工作簿
            try (Workbook workbook = new SXSSFWorkbook(1000)) {

                // 1. 创建统计信息Sheet
                createStatisticsSheet(workbook, totalCount, successCount, failCount);

                // 2. 创建错误记录Sheet（如果有错误）
                if (!errorRecords.isEmpty()) {
                    createErrorRecordsSheet(workbook, errorRecords);
                }

                // 3. 写入文件
                try (FileOutputStream fos = new FileOutputStream(resultFile)) {
                    workbook.write(fos);
                }

                // 4. 上传到OSS
                return uploadResultFile(resultFile, appId, registerId);

            } finally {
                // 清理临时文件
                if (resultFile.exists()) {
                    resultFile.delete();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("生成结果文件失败", e);
        }
    }

    /**
     * 创建统计信息Sheet
     */
    private void createStatisticsSheet(Workbook workbook, long totalCount,
                                     long successCount, long failCount) {
        Sheet sheet = workbook.createSheet("导入统计");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("统计项");
        headerRow.createCell(1).setCellValue("数量");
        headerRow.createCell(2).setCellValue("百分比");

        // 添加统计行
        Row totalRow = sheet.createRow(1);
        totalRow.createCell(0).setCellValue("总记录数");
        totalRow.createCell(1).setCellValue(totalCount);
        totalRow.createCell(2).setCellValue("100%");

        Row successRow = sheet.createRow(2);
        successRow.createCell(0).setCellValue("成功记录数");
        successRow.createCell(1).setCellValue(successCount);
        successRow.createCell(2).setCellValue(String.format("%.2f%%", (successCount * 100.0 / totalCount)));

        Row failRow = sheet.createRow(3);
        failRow.createCell(0).setCellValue("失败记录数");
        failRow.createCell(1).setCellValue(failCount);
        failRow.createCell(2).setCellValue(String.format("%.2f%%", (failCount * 100.0 / totalCount)));

        // 设置样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
    }

    /**
     * 创建错误记录Sheet
     */
    private void createErrorRecordsSheet(Workbook workbook, List<ErrorRecord> errorRecords) {
        Sheet sheet = workbook.createSheet("错误记录");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("行号");
        headerRow.createCell(1).setCellValue("错误信息");
        headerRow.createCell(2).setCellValue("建议操作");

        // 添加错误记录
        for (int i = 0; i < errorRecords.size() && i < 10000; i++) { // 最多记录10000条
            ErrorRecord error = errorRecords.get(i);
            Row row = sheet.createRow(i + 1);
            Object cellValue = error.getRowNumber() != null ?
                error.getRowNumber().doubleValue() : (error.getRowKey() != null ? error.getRowKey() : "");
            row.createCell(0).setCellValue(cellValue.toString());
            row.createCell(1).setCellValue(error.getErrorMessage());
            row.createCell(2).setCellValue(error.getSuggestedAction());
        }

        // 设置样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
    }

    /**
     * 创建临时结果文件
     */
    private File createTempResultFile(Long registerId) {
        String tempDir = TEMP_DIR;
        new File(tempDir).mkdirs();

        String fileName = String.format("import_result_%s_%s.xlsx",
            registerId,
            System.currentTimeMillis());

        return new File(tempDir, fileName);
    }

    /**
     * 上传到OSS
     */
    private String uploadResultFile(File file, String appId, Long registerId) {
        try {
            Pair<String, String> uploadResult = uploadService.upload(file, file.getName(), appId);
            return uploadResult.getValue();
        } catch (Exception e) {
            throw new RuntimeException("上传结果文件失败", e);
        }
    }
}
