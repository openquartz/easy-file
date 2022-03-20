package org.svnee.easyfile.server.service;

import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;

/**
 * 异步下载任务服务
 *
 * @author svnee
 */
public interface AsyncDownloadTaskService {

    /**
     * 自动注册
     *
     * @param request 请求
     */
    void autoRegister(AutoTaskRegisterRequest request);

}
