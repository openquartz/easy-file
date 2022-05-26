package org.svnee.easyfile.storage.expand;

import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.request.ExportLimitingRequest;

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
