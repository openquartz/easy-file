package org.svnee.easyfile.example.excel;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.example.utils.WaterMarkExcelUtil;
import org.svnee.easyfile.core.executor.excel.ExcelIntensifier;

/**
 * 增强水印
 * @author svnee
 **/
@Slf4j
public class WaterMarkExcelIntensifier implements ExcelIntensifier {

    @Override
    public void enhance(Workbook workbook, BaseDownloaderRequestContext context) {

        if (workbook instanceof XSSFWorkbook) {
            try {
                WaterMarkExcelUtil.printWaterMark((XSSFWorkbook) workbook, "敏感内容");
            } catch (IOException e) {
                log.error("[WaterMarkExcelIntensifier#enhance]", e);
            }
        } else if (workbook instanceof SXSSFWorkbook) {
            try {
                WaterMarkExcelUtil.printWaterMark((SXSSFWorkbook) workbook, "敏感内容");
            } catch (IOException e) {
                log.error("[WaterMarkExcelIntensifier#enhance]", e);
            }
        } else {
            throw new RuntimeException("this workbook not support watermark!");
        }
    }
}
