package org.svnee.easyfile.starter.executor.impl;

import javax.servlet.http.HttpServletResponse;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.BaseWrapperSyncResponseHeader;

/**
 * 07版Excel 下载执行器
 *
 * @author svnee
 */
public abstract class AbstractDownloadExcel07Executor implements BaseDownloadExecutor, BaseWrapperSyncResponseHeader {

    @Override
    public void setSyncResponseHeader(HttpServletResponse response) {
        // 设置请求头
        ExportsResponseHeaderWrapper.wrapperExcel07(response, "exports");
    }

}
