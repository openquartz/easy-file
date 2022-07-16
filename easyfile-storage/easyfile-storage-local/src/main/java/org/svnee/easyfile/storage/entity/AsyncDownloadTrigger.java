package org.svnee.easyfile.storage.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.svnee.easyfile.storage.dictionary.DownloadTriggerStatusEnum;

/**
 * 异步下载触发器
 *
 * @author svnee
 **/
@Data
@Table(name = "ef_async_download_register")
public class AsyncDownloadTrigger {

    /**
     * Id
     */
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 注册ID
     *
     * @see AsyncDownloadRecord#getId()
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

}
