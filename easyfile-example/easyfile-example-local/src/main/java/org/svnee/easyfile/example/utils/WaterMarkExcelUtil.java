package org.svnee.easyfile.example.utils;

import cn.hutool.core.util.ReflectUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 水印工具类
 *
 * @author svnee
 **/
public final class WaterMarkExcelUtil {

    private WaterMarkExcelUtil() {
    }

    public static void printWaterMark(XSSFWorkbook workbook, String content) throws IOException {

        BufferedImage image = FontImage.createWatermarkImage(content, "yyyy-MM-dd HH:mm:ss", "#C5CBCF");
        // 导出到字节流B
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);

        int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        XSSFPictureData pictureData = workbook.getAllPictures().get(pictureIdx);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            //add relation from sheet to the picture data
            String rID = sheet.addRelation(null, XSSFRelation.IMAGES, pictureData).getRelationship().getId();
            // String rid = sheet.addRelation(null,XSSFRelation.IMAGES,workbook.getAllPictures().get(pictureIdx))
            //set background picture to sheet
            sheet.getCTWorksheet().addNewPicture().setId(rID);
        }
    }

    public static void printWaterMark(SXSSFWorkbook workbook, String content) throws IOException {

        BufferedImage image = FontImage.createWatermarkImage(content, "yyyy-MM-dd HH:mm:ss", "#C5CBCF");
        // 导出到字节流B
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);

        int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);

        XSSFPictureData pictureData = (XSSFPictureData) workbook.getAllPictures().get(pictureIdx);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            SXSSFSheet sheet = workbook.getSheetAt(i);
            XSSFSheet xssfSheet = (XSSFSheet) ReflectUtil.getFieldValue(sheet, "_sh");
            //add relation from sheet to the picture data
            String rID = xssfSheet.addRelation(null, XSSFRelation.IMAGES, pictureData).getRelationship().getId();
            // String rid = sheet.addRelation(null,XSSFRelation.IMAGES,workbook.getAllPictures().get(pictureIdx))
            //set background picture to sheet
            xssfSheet.getCTWorksheet().addNewPicture().setId(rID);
        }
    }

}
