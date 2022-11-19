package org.svnee.easyfile.common.constants;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

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
    public static final String UTF_8 = StandardCharsets.UTF_8.toString();

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

    /**
     * Health Check Interval
     */
    public static final int HEALTH_CHECK_INTERVAL = 5;

    /**
     * 服务前缀
     */
    public static final String BASE_PATH = "/easyfile";

    /**
     * Health Check
     */
    public static final String HEALTH_CHECK_PATH = BASE_PATH + "/health/check";

    /**
     * Server UP
     */
    public static final String UP = "UP";

    /**
     * Access_Token
     */
    public static final String ACCESS_TOKEN = "accessToken";

    /**
     * SSL
     */
    public static final String SSL = "SSL";

    /**
     * 分隔符
     */
    public static final String COLON_SPLITTER = ":";

    /**
     * 逗号分割符号
     */
    public static final String COMMA_SPLITTER = ",";

    /**
     * 流式导出增强推荐长度
     */
    public static final Integer STREAM_EXPORT_ENHANCE_RECOMMEND_LEN = 500;

    /**
     * one MB
     */
    public static final BigInteger ONE_MB = BigInteger.valueOf(1048576);

    /**
     * one KB
     */
    public static final BigInteger ONE_KB = BigInteger.valueOf(1024);

    /**
     * easy-file sender
     */
    public static final String EASY_FILE_SENDER = "EasyFileSender";


}
