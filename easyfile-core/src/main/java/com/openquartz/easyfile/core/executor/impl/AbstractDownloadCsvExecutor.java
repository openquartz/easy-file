package com.openquartz.easyfile.core.executor.impl;

import javax.servlet.http.HttpServletResponse;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;

/**
 * Csv 下载执行器
 *
 * @author svnee
 */
public abstract class AbstractDownloadCsvExecutor implements BaseDownloadExecutor, BaseWrapperSyncResponseHeader {

    @Override
    public void setSyncResponseHeader(HttpServletResponse response) {
        ExportsResponseHeaderWrapper.wrapperCsv(response, "exports");
    }

}
