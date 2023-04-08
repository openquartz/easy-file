package com.openquartz.easyfile.storage.download;

import com.openquartz.easyfile.common.request.ExportLimitingRequest;

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
