package org.svnee.easyfile.common.constants;

/**
 * 常量类
 *
 * @author svnee
 */
public final class Constants {

    private Constants() {
    }

    /**
     * 文件后缀名分隔符
     */
    public static final String FILE_SUFFIX_SEPARATOR = ".";

    /**
     * 最大上传文件失败信息长度
     */
    public static final Integer MAX_UPLOAD_ERROR_MSG_LENGTH = 200;

    /**
     * 无系统
     */
    public static final String NONE_FILE_SYSTEM = "NONE";

    /**
     * 默认限流策略
     */
    public static final String DEFAULT_LIMITING_STRATEGY = "NONE";

    /**
     * 初始化版本号
     */
    public static final int DATA_INIT_VERSION = 1;

    /**
     * 系统用户
     */
    public static final String SYSTEM_USER = "system";

    /**
     * GMT+8
     */
    public static final String GMT8 = "GMT+8";

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * response-header-content
     */
    public static final String RESPONSE_HEADER_CONTENT = "Content-Disposition";

    /**
     * response-attachment-prefix
     */
    public static final String RESPONSE_ATTACHMENT_PREFIX = "attachment;filename=";

    /**
     * Response Header Content-Type Excel
     */
    public static final String RESPONSE_HEADER_CONTENT_TYPE_EXCEL = "application/msexcel";

    /**
     * Response Header Content-Type CSV
     */
    public static final String RESPONSE_HEADER_CONTENT_TYPE_CSV = "text/csv;charset=utf-8";


}
