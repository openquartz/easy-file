package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import javax.servlet.http.HttpServletResponse;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;

/**
 * Csv 下载执行器
 *
 * @author svnee
 */
public abstract class AbstractExportCsvExecutor implements BaseExportExecutor, BaseWrapperSyncResponseHeader {

    @Override
    public void setSyncResponseHeader(HttpServletResponse response) {
        ExportsResponseHeaderWrapper.wrapperCsv(response, "exports");
    }

}
