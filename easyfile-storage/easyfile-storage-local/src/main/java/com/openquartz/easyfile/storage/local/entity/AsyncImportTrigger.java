package com.openquartz.easyfile.storage.local.entity;

import com.openquartz.easyfile.storage.local.dictionary.DownloadTriggerStatusEnum;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * 异步导入触发器
 *
 * @author svnee
 */
@Data
@Table(name = "ef_async_import_trigger")
public class AsyncImportTrigger {

    /**
     * Id
     */
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 注册ID
     */
    @Column(name = "register_id")
    private Long registerId;

    /**
     * 触发状态
     */
    @Column(name = "trigger_status")
    private DownloadTriggerStatusEnum triggerStatus;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private Date startTime;

    /**
     * 最新执行时间
     */
    @Column(name = "last_execute_time")
    private Date lastExecuteTime;

    /**
     * 触发次数
     */
    @Column(name = "trigger_count")
    private Integer triggerCount;

    /**
     * 创建者-owner (IP)
     */
    @Column(name = "creating_owner")
    private String creatingOwner;

    /**
     * 正在执行owner (IP)
     */
    @Column(name = "processing_owner")
    private String processingOwner;
}
