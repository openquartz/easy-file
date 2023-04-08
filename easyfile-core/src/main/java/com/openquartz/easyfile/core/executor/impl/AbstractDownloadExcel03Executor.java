package com.openquartz.easyfile.core.executor.impl;

import javax.servlet.http.HttpServletResponse;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;

/**
 * 07版Excel 下载执行器
 *
 * @author svnee
 */
public abstract class AbstractDownloadExcel03Executor implements BaseDownloadExecutor, BaseWrapperSyncResponseHeader {

    @Override
    public void setSyncResponseHeader(HttpServletResponse response) {
        // 设置请求头
        ExportsResponseHeaderWrapper.wrapperExcel03(response, "exports");
    }

}
