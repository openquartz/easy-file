package org.svnee.easyfile.server.service.executor;

import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.request.ExportLimitingRequest;

/**
 * @author svnee
 **/
@Component
public class NoneExportLimitingExecutor implements ExportLimitingExecutor {

    @Override
    public String strategy() {
        return Constants.DEFAULT_LIMITING_STRATEGY;
    }

    @Override
    public void limit(ExportLimitingRequest request) {
    }
}
