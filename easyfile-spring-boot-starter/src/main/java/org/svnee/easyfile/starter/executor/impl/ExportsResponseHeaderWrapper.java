package org.svnee.easyfile.starter.executor.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.FileSuffixEnum;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.starter.exception.DownloadErrorCode;

/**
 * 导出请求头包装类
 *
 * @author svnee
 */
public final class ExportsResponseHeaderWrapper {

    private ExportsResponseHeaderWrapper() {
    }

    /**
     * 包装Excel07
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperExcel07(HttpServletResponse response, String fileName) {
        try {
            response.setHeader(Constants.RESPONSE_HEADER_CONTENT,
                Constants.RESPONSE_ATTACHMENT_PREFIX + URLEncoder.encode(fileName, Constants.UTF_8)
                    + FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        } catch (UnsupportedEncodingException e) {
            throw new EasyFileException(DownloadErrorCode.DOWNLOAD_FILE_NAME_ENCODING_ERROR);
        }
        response.setContentType(Constants.RESPONSE_HEADER_CONTENT_TYPE_EXCEL);
    }

    /**
     * 包装成 Excel03
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperExcel03(HttpServletResponse response, String fileName) {
        try {
            response.setHeader(Constants.RESPONSE_HEADER_CONTENT,
                Constants.RESPONSE_ATTACHMENT_PREFIX + URLEncoder.encode(fileName, Constants.UTF_8)
                    + FileSuffixEnum.EXCEL_03.getFullFileSuffix());
        } catch (UnsupportedEncodingException e) {
            throw new EasyFileException(DownloadErrorCode.DOWNLOAD_FILE_NAME_ENCODING_ERROR);
        }
        response.setContentType(Constants.RESPONSE_HEADER_CONTENT_TYPE_EXCEL);
    }

    /**
     * 包装成CSV
     *
     * @param response response
     * @param fileName fileName
     */
    public static void wrapperCsv(HttpServletResponse response, String fileName) {
        try {
            response.setHeader(Constants.RESPONSE_HEADER_CONTENT,
                Constants.RESPONSE_ATTACHMENT_PREFIX + URLEncoder.encode(fileName, Constants.UTF_8)
                    + FileSuffixEnum.CSV.getFullFileSuffix());
        } catch (UnsupportedEncodingException e) {
            throw new EasyFileException(DownloadErrorCode.DOWNLOAD_FILE_NAME_ENCODING_ERROR);
        }
        response.setContentType(Constants.RESPONSE_HEADER_CONTENT_TYPE_CSV);
    }
}
