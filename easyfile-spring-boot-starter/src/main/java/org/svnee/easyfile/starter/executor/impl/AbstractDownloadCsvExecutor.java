package org.svnee.easyfile.starter.executor.impl;

import javax.servlet.http.HttpServletResponse;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.BaseWrapperSyncResponseHeader;

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
