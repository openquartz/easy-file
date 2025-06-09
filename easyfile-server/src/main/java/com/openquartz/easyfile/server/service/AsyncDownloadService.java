package com.openquartz.easyfile.server.service;

import java.util.List;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
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
 * 异步下载任务服务
 *
 * @author svnee
 */
public interface AsyncDownloadService {

    /**
     * 注册下载记录
     *
     * @param request 注册下载请求
     * @return 下载记录ID
     */
    Long register(RegisterDownloadRequest request);

    /**
     * 限流请求
     *
     * @param request 限流请求
     * @return 请求成功或失败
     */
    boolean limiting(ExportLimitingRequest request);

    /**
     * 上传回馈
     *
     * @param request 上传请求
     */
    void uploadCallback(UploadCallbackRequest request);

    /**
     * 校验运行
     *
     * @param registerId 注册ID
     * @return 是否可以运行
     */
    boolean enableRunning(Long registerId);

    /**
     * 导出结果查询
     *
     * @param request 查询请求
     * @return 查询结果
     */
    Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request);

    /**
     * 加载APP ID
     *
     * @return All APP ID
     */
    List<String> loadAllAppId();

    /**
     * 失效链接下载
     */
    void invalid();

    /**
     * 下载次数
     *
     * @param request 请求
     * @return file url
     * @since 1.2.1
     */
    DownloadUrlResult download(DownloadRequest request);

    /**
     * 撤销
     *
     * @param request 请求
     * @return 撤销上传任务
     */
    CancelUploadResult cancel(CancelUploadRequest request);

    /**
     * 导出缓存结果
     *
     * @param request 导出缓存请求
     * @return 导出结果
     */
    ExportResult loadingExportCacheResult(LoadingExportCacheRequest request);

    /**
     * 重置执行进度
     *
     * @param registerId 注册ID
     */
    void resetExecuteProcess(Long registerId);

    /**
     * 刷新执行进度
     *
     * @param registerId 注册ID
     * @param executeProcess 执行进度
     * @param nextUploadStatus 下一个上传处理状态
     */
    void refreshExecuteProcess(Long registerId, Integer executeProcess,
        HandleStatusEnum nextUploadStatus);

    /**
     * 获取注册下载的请求信息
     *
     * @param registerId 注册任务ID
     * @return 请求信息
     */
    DownloadRequestInfo getRequestInfoByRegisterId(Long registerId);

    /**
     * app tree
     *
     * @return app tree
     */
    List<AppTree> getAppTree();

    /**
     * 获取语言
     * @param registerId registerId
     * @return locale
     */
    String getLocale(Long registerId);
}
