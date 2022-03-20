package org.svnee.easyfile.server.bean.request;

import org.svnee.easyfile.common.bean.BaseExecuteParam;

/**
 * 重试执行参数
 *
 * @author svnee
 */
public class RetryExecuteRequest extends BaseExecuteParam {

    /**
     * 注册ID
     */
    private Long registerId;

    public Long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
    }
}
