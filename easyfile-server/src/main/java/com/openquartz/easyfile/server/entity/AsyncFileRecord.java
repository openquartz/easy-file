package com.openquartz.easyfile.server.entity;

import com.openquartz.easyfile.common.bean.BaseExecuteParam;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

import java.util.Date;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;

/**
 * 异步下载记录
 *
 * @author svnee
 */
@Data
@Table(name = "ef_async_file_record")
public class AsyncFileRecord {

    @Id
    private Long id;

    /**
     * 下载任务ID
     *
     * @see AsyncFileTask#getId()
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * app ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 下载code
     */
    @Column(name = "executor_code")
    private String executorCode;

    /**
     * 上传状态
     */
    @Column(name = "handle_status")
    private HandleStatusEnum handleStatus;

    /**
     * 文件路径
     */
    @Column(name = "file_url")
    private String fileUrl;

    /**
     * fileName
     *
     * @since 1.2.1
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 文件所在系统
     */
    @Column(name = "file_system")
    private String fileSystem;

    /**
     * 下载操作人
     */
    @Column(name = "operate_by")
    private String operateBy;

    /**
     * 下载操作人
     */
    @Column(name = "operate_name")
    private String operateName;

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
     *
     * @see BaseExecuteParam
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
     * 下载次数
     */
    @Column(name = "download_num")
    private Integer downloadNum;

    /**
     * 执行进度
     */
    @Column(name = "execute_process")
    private Integer executeProcess;

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
     * 是否已经上传完成
     *
     * @return 是否已经完成
     */
    public boolean isUploadComplete() {
        return !(HandleStatusEnum.NONE == handleStatus
            || HandleStatusEnum.EXECUTING == handleStatus
            || HandleStatusEnum.UPLOADING == handleStatus);
    }

}
