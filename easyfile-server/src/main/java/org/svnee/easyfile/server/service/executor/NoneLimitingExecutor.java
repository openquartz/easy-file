package org.svnee.easyfile.server.service.executor;

import static org.svnee.easyfile.server.service.executor.LimitingConstants.LIMITING_PREFIX;

import lombok.RequiredArgsConstructor;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.server.common.spi.SpringSpi;

/**
 * 不做限流
 *
 * @author svnee
 */
@RequiredArgsConstructor
@SpringSpi(type = ExportLimitingExecutor.class, providerName = LIMITING_PREFIX + LimitingConstants.NONE)
public class NoneLimitingExecutor implements ExportLimitingExecutor {

    @Override
    public boolean limit(ExportLimitingRequest request) {
        return true;
    }
}
