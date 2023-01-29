package org.svnee.easyfile.common.request;

import lombok.Data;

/**
 * 刷新执行进度请求
 *
 * @author svnee
 **/
@Data
public class RefreshExecuteProcessRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 执行进度
     */
    private Integer executeProcess;

}
