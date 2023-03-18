package org.svnee.easyfile.core.executor;

import javax.servlet.http.HttpServletResponse;

/**
 * wrapper 同步请求头
 * 当下载为同步执行器时,包装同步返回头
 *
 * @author svnee
 */
public interface BaseWrapperSyncResponseHeader {

    /**
     * 设置同步请求头执行器
     *
     * @param response 当前请求response
     */
    default void setSyncResponseHeader(HttpServletResponse response) {
    }
}
