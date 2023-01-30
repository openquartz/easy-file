package org.svnee.easyfile.storage.download;

import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.ExportResult;

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
     * 加载导出缓存结果
     *
     * @param request 加载导出缓存请求
     * @return 缓存结果
     */
    ExportResult loadingCacheExportResult(LoadingExportCacheRequest request);

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

    /**
     * 自动注册下载任务
     *
     * @param request 请求
     */
    void autoRegisterTask(AutoTaskRegisterRequest request);

    /**
     * 下載
     *
     * @param request 下载请求
     * @return 下载文件url
     */
    String download(DownloadRequest request);

    /**
     * 用户撤销任务上传
     *
     * @param request 请求
     * @return 请求
     */
    CancelUploadResult cancelUpload(CancelUploadRequest request);

    /**
     * 注册ID
     *
     * @param registerId registerId
     * @return 下载请求信息
     */
    DownloadRequestInfo getRequestInfoByRegisterId(Long registerId);

    /**
     * 更新执行进度
     *
     * @param registerId 注册ID
     * @param executeProcess 执行进度
     * @param nextUploadStatus 下一个状态
     */
    void refreshExecuteProgress(Long registerId, Integer executeProcess, UploadStatusEnum nextUploadStatus);

    /**
     * 重置执行进度
     *
     * @param registerId registerId
     */
    void resetExecuteProcess(Long registerId);
}
