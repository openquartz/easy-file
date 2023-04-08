package com.openquartz.easyfile.example.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FontBufferedImage
 *
 * @author svnee
 */
public final class FontImage {

    private FontImage() {
    }

    public static BufferedImage createWatermarkImage(String text, String dateFormat, String color) {

        String[] textArray = text.split("\n");
        Font font = new Font("microsoft-yahei", Font.PLAIN, 20);
        int width = 600;
        int height = 200;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 背景透明 开始
        Graphics2D g = image.createGraphics();
        image = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g.dispose();
        // 背景透明 结束
        g = image.createGraphics();
        // 设定画笔颜色
        g.setColor(new Color(Integer.parseInt(color.substring(1), 16)));
        // 设置画笔字体
        g.setFont(font);
        // 设定倾斜度
        g.shear(0.1, -0.26);

        //        设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 50;
        // 画出字符串
        for (String s : textArray) {
            g.drawString(s, 0, y);
            y = y + font.getSize();
        }
        //g.drawString(DateUtils.getNowDateFormatCustom(watermark.getDateFormat()), 0, y);// 画出字符串
        // 画出字符串
        String format = new SimpleDateFormat(dateFormat).format(new Date());
        g.drawString(format, 0, y);

        g.dispose();// 释放画笔
        return image;
    }

}