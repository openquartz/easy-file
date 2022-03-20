package org.svnee.easyfile.server.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

import java.util.Date;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;

/**
 * 异步下载记录
 *
 * @author svnee
 */
@Data
@Table(name = "ef_async_download_record")
public class AsyncDownloadRecord {

    @Id
    private Long id;

    /**
     * 下载任务ID
     *
     * @see AsyncDownloadTask#getId()
     */
    @Column(name = "download_task_id")
    private Long downloadTaskId;

    /**
     * app ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 下载code
     */
    @Column(name = "download_code")
    private String downloadCode;

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
     * 文件所在系统
     */
    @Column(name = "file_system")
    private String fileSystem;

    /**
     * 下载操作人
     */
    @Column(name = "download_operate_by")
    private String downloadOperateBy;

    /**
     * 下载操作人
     */
    @Column(name = "download_operate_name")
    private String downloadOperateName;

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
     * @see org.svnee.easyfile.common.bean.BaseExecuteParam
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

}
