package com.openquartz.easyfile.storage.local.entity;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
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
     */
    @Column(name = "import_task_id")
    private Long importTaskId;

    /**
     * app ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 导入code
     */
    @Column(name = "import_code")
    private String importCode;

    /**
     * 上传状态
     */
    @Column(name = "upload_status")
    private UploadStatusEnum uploadStatus;

    /**
     * 文件路径
     */
    @Column(name = "file_url")
    private String fileUrl;

    /**
     * 文件名
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 文件所在系统
     */
    @Column(name = "file_system")
    private String fileSystem;

    /**
     * 导入操作人
     */
    @Column(name = "import_operate_by")
    private String importOperateBy;

    /**
     * 导入操作人姓名
     */
    @Column(name = "import_operate_name")
    private String importOperateName;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 通知启用状态
     */
    @Column(name = "notify_enable_status")
    private Integer notifyEnableStatus;

    /**
     * 通知有效
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
     * 重试执行参数
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
     * 链接失效时间
     */
    @Column(name = "invalid_time")
    private Date invalidTime;

    /**
     * 执行进度
     */
    @Column(name = "execute_process")
    private Integer executeProcess;

    /**
     * 失败文件路径
     */
    @Column(name = "error_file_url")
    private String errorFileUrl;

    /**
     * 成功行数
     */
    @Column(name = "success_rows")
    private Integer successRows;

    /**
     * 失败行数
     */
    @Column(name = "fail_rows")
    private Integer failRows;

    /**
     * 总行数
     */
    @Column(name = "total_rows")
    private Integer totalRows;

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
