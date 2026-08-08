package com.openquartz.easyfile.core.context;

import lombok.Data;

/**
 * 导入请求上下文（用户调用入口，对称 DownloaderRequestContext）
 *
 * @author svnee
 */
@Data
public class ImportRequestContext {

    /**
     * 注册ID（同步回落/已注册场景）
     */
    private Long registerId;

    /**
     * 导入编码（可选，通常由注解/注册表解析）
     */
    private String importCode;

    /**
     * 源文件 URL（上传完成后传入）
     */
    private String sourceFileUrl;

    /**
     * 源文件名
     */
    private String fileName;

    /**
     * 文件类型 (xlsx, xls, csv, json)
     */
    private String fileType;

    /**
     * 业务唯一键（幂等）
     */
    private String businessUniqueKey;

    /**
     * 执行参数 (JSON)
     */
    private String executeParam;

    /**
     * 通知邮箱
     */
    private String notifyEmail;

    /**
     * 语言区域
     */
    private String locale;

    /**
     * 操作人
     */
    private String operator;
}
