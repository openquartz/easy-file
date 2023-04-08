package com.openquartz.easyfile.server.service;

import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;

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
