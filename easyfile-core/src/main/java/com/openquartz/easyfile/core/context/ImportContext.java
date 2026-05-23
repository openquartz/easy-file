package com.openquartz.easyfile.core.context;

import com.openquartz.easyfile.common.bean.Notifier;
import lombok.Builder;
import lombok.Data;

/**
 * 导入上下文
 * 包含导入任务的所有上下文信息
 *
 * @author svnee
 */
@Data
@Builder
public class ImportContext {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 导入编码
     */
    private String importCode;

    /**
     * OSS文件URL
     */
    private String ossUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型 (xlsx, xls, csv, json)
     */
    private String fileType;

    /**
     * 文件哈希 (MD5, 用于幂等性检查)
     */
    private String fileHash;

    /**
     * 业务唯一键 (用于幂等性检查)
     */
    private String businessUniqueKey;

    /**
     * 执行参数 (JSON格式)
     */
    private String executeParam;

    /**
     * 通知人信息
     */
    private Notifier notifier;

    /**
     * 是否启用通知
     */
    private Boolean enableNotify;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 当前重试次数
     */
    private Integer currentRetry = 0;

    /**
     * 断点续传位置
     */
    private Long resumePosition = 0L;

    /**
     * 创建人
     */
    private String createBy;
}
