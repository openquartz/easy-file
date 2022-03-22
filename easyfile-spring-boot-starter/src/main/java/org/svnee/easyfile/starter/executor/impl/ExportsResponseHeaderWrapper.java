package org.svnee.easyfile.starter.executor.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import org.svnee.easyfile.common.constants.Constants;

/**
 * 导出请求头包装类
 *
 * @author svnee
 */
public class ExportsResponseHeaderWrapper {

    /**
     * 包装Excel07
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperExcel07(HttpServletResponse response, String fileName) {
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, Constants.UTF8) + ".xlsx");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("export error:" + e.getMessage());
        }
        response.setContentType("application/msexcel");
    }

    /**
     * 包装成 Excel03
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperExcel03(HttpServletResponse response, String fileName) {
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, Constants.UTF8) + ".xls");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("export error:" + e.getMessage());
        }
        response.setContentType("application/msexcel");
    }

    /**
     * 包装成CSV
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperCsv(HttpServletResponse response, String fileName) {
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, Constants.UTF8) + ".csv");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("export error:" + e.getMessage());
        }
        response.setContentType("text/csv;charset=utf-8");
    }
}
