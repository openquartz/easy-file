package org.svnee.easyfile.storage.download;

import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;

/**
 * 下载存储服务
 *
 * @author svnee
 */
public interface DownloadStorageService {

    /**
     * 开启执行下载服务
     *
     * @param registerId 注册下载ID
     * @return 是否启用运行成功
     */
    boolean enableRunning(Long registerId);

    /**
     * 更新上传回调结果
     *
     * @param request 请求
     */
    void uploadCallback(UploadCallbackRequest request);

    /**
     * 注册下载请求
     *
     * @param downloadRequest 下载请求
     * @return 注册下载ID
     */
    Long register(RegisterDownloadRequest downloadRequest);


}
