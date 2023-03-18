package org.svnee.easyfile.storage;

import java.util.List;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.util.page.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.EnableRunningRequest;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.AppTree;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.DownloadUrlResult;
import org.svnee.easyfile.common.response.ExportResult;

/**
 * EasyFileClient
 * @author svnee
 */
public interface EasyFileClient {

    /**
     * 注册下载任务
     *
     * @param request 注册下载请求
     * @return 返回下载记录ID
     */
    ResponseResult<Long> register(RegisterDownloadRequest request);

    /**
     * 自动注册任务
     *
     * @param request 注册请求
     * @return 注册结果
     */
    ResponseResult<?> autoRegisterTask(AutoTaskRegisterRequest request);

    /**
     * 下载任务回传
     *
     * @param request 上传文件回传结果
     * @return 上传
     */
    ResponseResult<?> uploadCallback(UploadCallbackRequest request);

    /**
     * 导出限流
     *
     * @param request 导出限流请求
     * @return 限流结果
     */
    ResponseResult<?> limiting(ExportLimitingRequest request);

    /**
     * 运行下载
     *
     * @param request 运行请求
     * @return 开启运行结果
     */
    ResponseResult<Boolean> enableRunning(EnableRunningRequest request);

    /**
     * 下载导出结果
     *
     * @param request 请求
     * @return 导出结果
     */
    ResponseResult<Pagination<DownloadResult>> listExportResult(ListDownloadResultRequest request);

    /**
     * 下载
     *
     * @param request 下载请求
     * @return ResponseResult-fileUrl
     */
    ResponseResult<DownloadUrlResult> download(DownloadRequest request);

    /**
     * 用户撤销任务上传
     *
     * @param request 请求
     * @return 请求
     */
    ResponseResult<CancelUploadResult> cancelUpload(CancelUploadRequest request);

    /**
     * 加载导出缓存结果
     *
     * @param request 请求
     * @return 缓存导出结果
     */
    ResponseResult<ExportResult> loadingExportCacheResult(LoadingExportCacheRequest request);

    /**
     * 根据registerId查询下载请求信息
     *
     * @param registerId 注册ID
     * @return 下载请求信息
     */
    ResponseResult<DownloadRequestInfo> getRequestInfoByRegisterId(Long registerId);

    /**
     * 重置执行进度
     *
     * @param registerId 注册ID
     * @return response
     */
    ResponseResult<?> resetExecuteProcess(Long registerId);

    /**
     * 刷新执行进度
     *
     * @param registerId 注册ID
     * @param executeProcess 执行进度
     * @param nextUploadStatus next upload status
     * @return response
     */
    ResponseResult<?> refreshExecuteProcess(Long registerId, Integer executeProcess,
        UploadStatusEnum nextUploadStatus);

    /**
     * get app tree
     *
     * @return app
     */
    ResponseResult<List<AppTree>> getAppTree();
}
