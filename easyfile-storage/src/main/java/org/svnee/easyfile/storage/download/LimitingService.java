package org.svnee.easyfile.storage.download;

import org.svnee.easyfile.common.request.ExportLimitingRequest;

/**
 * 限流服务
 *
 * @author svnee
 */
public interface LimitingService {

    /**
     * 限流
     *
     * @param request 导出限流请求
     */
    void limiting(ExportLimitingRequest request);


}
