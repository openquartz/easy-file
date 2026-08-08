package com.openquartz.easyfile.storage.download;

import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;

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

    /**
     * 导入限流（默认空实现，本地/远程可覆盖）
     *
     * @param request 导入限流请求
     */
    default void limiting(ImportLimitingRequest request) {
    }

}
