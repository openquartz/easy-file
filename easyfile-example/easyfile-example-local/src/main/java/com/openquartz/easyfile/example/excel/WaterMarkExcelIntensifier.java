package com.openquartz.easyfile.example.excel;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.example.utils.WaterMarkExcelUtil;
import com.openquartz.easyfile.core.executor.excel.ExcelIntensifier;

/**
 * 增强水印
 * @author svnee
 **/
@Slf4j
public class WaterMarkExcelIntensifier implements ExcelIntensifier {

    @Override
    public void enhance(Workbook workbook, BaseExportRequestContext context) {

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
