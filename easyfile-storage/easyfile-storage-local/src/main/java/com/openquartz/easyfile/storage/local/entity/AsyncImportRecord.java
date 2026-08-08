package com.openquartz.easyfile.storage.local.entity;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * 异步导入记录
 *
 * @author svnee
 */
@Data
@Table(name = "ef_async_import_record")
public class AsyncImportRecord {

    @Id
    private Long id;

    /**
     * 导入任务ID
     *
     * @see AsyncImportTask#getId()
     */
    @Column(name = "import_task_id")
    private Long importTaskId;

    /**
     * app ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 导入 code
     */
    @Column(name = "import_code")
    private String importCode;

    /**
     * 导入状态
     */
    @Column(name = "import_status")
    private ImportStatusEnum importStatus;

    /**
     * 源文件路径（对应 ImportRegisterRequest.ossUrl）
     */
    @Column(name = "source_file_url")
    private String sourceFileUrl;

    /**
     * 源文件名
     */
    @Column(name = "source_file_name")
    private String sourceFileName;

    /**
     * 文件类型
     */
    @Column(name = "file_type")
    private String fileType;

    /**
     * 文件哈希
     */
    @Column(name = "file_hash")
    private String fileHash;

    /**
     * 业务幂等键
     */
    @Column(name = "business_unique_key")
    private String businessUniqueKey;

    /**
     * 结果文件路径
     */
    @Column(name = "result_file_url")
    private String resultFileUrl;

    /**
     * 总行数
     */
    @Column(name = "total_count")
    private Long totalCount;

    /**
     * 成功行数
     */
    @Column(name = "success_count")
    private Long successCount;

    /**
     * 失败行数
     */
    @Column(name = "fail_count")
    private Long failCount;

    /**
     * 执行进度
     */
    @Column(name = "execute_process")
    private Integer executeProcess;

    /**
     * 断点续传位置
     */
    @Column(name = "resume_position")
    private Long resumePosition;

    /**
     * 通知启用状态
     */
    @Column(name = "notify_enable_status")
    private Integer notifyEnableStatus;

    /**
     * 通知邮箱
     */
    @Column(name = "notify_email")
    private String notifyEmail;

    /**
     * 最大服务重试
     */
    @Column(name = "max_server_retry")
    private Integer maxServerRetry;

    /**
     * 当前重试次数
     */
    @Column(name = "current_retry")
    private Integer currentRetry;

    /**
     * 导入执行参数
     */
    @Column(name = "execute_param")
    private String executeParam;

    /**
     * 异常信息
     */
    @Column(name = "error_msg")
    private String errorMsg;

    /**
     * 最新执行时间
     */
    @Column(name = "last_execute_time")
    private Date lastExecuteTime;

    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    private String createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by")
    private String updateBy;

    /**
     * 语言
     */
    @Column(name = "locale")
    private String locale;

}
