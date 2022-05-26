package org.svnee.easyfile.storage.expand;

import org.svnee.easyfile.common.request.ExportLimitingRequest;

/**
 * 限流服务
 *
 * @author svnee
 */
public interface ExportLimitingExecutor {

    /**
     * 策略
     *
     * @return 策略code码
     */
    String strategy();

    /**
     * 限流
     *
     * @param request request
     */
    void limit(ExportLimitingRequest request);

}