package com.openquartz.easyfile.storage.download;

import java.util.List;
import java.util.Locale;

import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;

/**
 * 下载存储服务
 *
 * @author svnee
 */
public interface FileTaskStorageService {

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
    DownloadUrlResult download(DownloadRequest request);

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
    void refreshExecuteProgress(Long registerId, Integer executeProcess, HandleStatusEnum nextUploadStatus);

    /**
     * 重置执行进度
     *
     * @param registerId registerId
     */
    void resetExecuteProcess(Long registerId);

    /**
     * 下载导出结果
     *
     * @param request 请求
     * @return 导出结果
     */
    Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request);

    /**
     * app tree
     *
     * @return app
     */
    List<AppTree> getAppTree();

    /**
     * 获取当前语言
     * @param registerId 注册ID
     * @return 当前语言
     */
    Locale getCurrentLocale(Long registerId);

}
