package org.svnee.easyfile.server.service.executor;

import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.server.common.spi.ServiceProvider;

/**
 * 限流服务
 *
 * @author svnee
 */
public interface ExportLimitingExecutor extends ServiceProvider {

    /**
     * 限流
     *
     * @param request 请求
     * @return 是否限流成功
     */
    boolean limit(ExportLimitingRequest request);

}
