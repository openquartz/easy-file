package com.openquartz.easyfile.storage.local.entity;

import javax.persistence.Column;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;

/**
 * 异步下载任务
 *
 * @author svnee
 */
@Data
@Table(name = "ef_async_download_task")
public class AsyncDownloadTask {

    @Id
    private Long id;

    /**
     * 任务编码
     */
    @Column(name = "task_code")
    private String taskCode;

    /**
     * 任务描述
     */
    @Column(name = "task_desc")
    private String taskDesc;

    /**
     * 归属系统 APP ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 统一APP ID
     */
    @Column(name = "unified_app_id")
    private String unifiedAppId;

    /**
     * 启用状态
     *
     * @see EnableStatusEnum#getCode()
     */
    @Column(name = "enable_status")
    private Integer enableStatus;

    /**
     * 限流策略
     */
    @Column(name = "limiting_strategy")
    private String limitingStrategy;

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
     * 是否删除
     */
    @Column(name = "is_deleted")
    private Long isDeleted;

}
