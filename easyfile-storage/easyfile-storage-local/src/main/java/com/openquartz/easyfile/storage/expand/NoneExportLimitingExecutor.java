package com.openquartz.easyfile.storage.expand;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;

/**
 * @author svnee
 **/
public class NoneExportLimitingExecutor implements ExportLimitingExecutor {

    @Override
    public String strategy() {
        return Constants.DEFAULT_LIMITING_STRATEGY;
    }

    @Override
    public void limit(ExportLimitingRequest request) {
    }
}
