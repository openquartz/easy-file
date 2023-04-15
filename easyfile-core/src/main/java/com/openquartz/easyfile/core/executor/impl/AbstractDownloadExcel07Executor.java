package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;
import javax.servlet.http.HttpServletResponse;

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
