package com.openquartz.easyfile.server.controller;

import com.openquartz.easyfile.server.service.AsyncDownloadService;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RefreshExecuteProcessRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;

/**
 * 下载controller
 *
 * @author svnee
 */
@RestController
@RequestMapping("/easyfile/download")
@RequiredArgsConstructor
public class DownloadController {

    private final AsyncDownloadService asyncDownloadService;

    /**
     * 限流
     */
    @PostMapping("/limiting")
    public ResponseResult<?> exportLimiting(@RequestBody @Valid ExportLimitingRequest request) {
        boolean limitResult = asyncDownloadService.limiting(request);
        if (limitResult) {
            return ResponseResult.ok();
        } else {
            return ResponseResult.fail("01", "超过限流标准");
        }
    }

    /**
     * 注册下载任务
     *
     * @param request 注册下载请求
     * @return 返回下载记录ID
     */
    @PostMapping("/register")
    public ResponseResult<Long> register(@RequestBody @Valid RegisterDownloadRequest request) {
        Long registerId = asyncDownloadService.register(request);
        return ResponseResult.ok(registerId);
    }

    /**
     * 下载任务回传
     *
     * @param request 上传文件回传结果
     * @return 上传
     */
    @PostMapping("/callback")
    public ResponseResult<?> uploadCallback(@RequestBody @Valid UploadCallbackRequest request) {
        asyncDownloadService.uploadCallback(request);
        return ResponseResult.ok();
    }

    /**
     * 用户撤销任务上传
     *
     * @param request 请求
     * @return 请求
     */
    @PostMapping("/cancel")
    public ResponseResult<CancelUploadResult> cancelUpload(@RequestBody @Valid CancelUploadRequest request) {
        CancelUploadResult result = asyncDownloadService.cancel(request);
        return ResponseResult.ok(result);
    }

    /**
     * 校验是否可运行
     *
     * @param request 校验运行请求
     * @return 运行请求
     */
    @PostMapping("/enableRunning")
    public ResponseResult<Boolean> enableRunning(@RequestBody @Valid EnableRunningRequest request) {
        boolean running = asyncDownloadService.enableRunning(request.getRegisterId());
        return ResponseResult.ok(running);
    }

    /**
     * 导出结果查询
     *
     * @param request 查询请求
     * @return 导出结果
     */
    @PostMapping("/listExport")
    public ResponseResult<Pagination<DownloadResult>> listExportResult(
        @RequestBody @Valid ListDownloadResultRequest request) {
        Pagination<DownloadResult> resultList = asyncDownloadService.listExportResult(request);
        return ResponseResult.ok(resultList);
    }

    /**
     * 下载次数
     *
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/file")
    public ResponseResult<DownloadUrlResult> download(@RequestBody @Valid DownloadRequest request) {
        DownloadUrlResult result = asyncDownloadService.download(request);
        return ResponseResult.ok(result);
    }

    /**
     * 加载导出缓存服务
     *
     * @param request request
     * @return 导出结果
     */
    @PostMapping("/loadingCache")
    public ResponseResult<ExportResult> loadingCache(@RequestBody @Valid LoadingExportCacheRequest request) {
        ExportResult exportResult = asyncDownloadService.loadingExportCacheResult(request);
        return ResponseResult.ok(exportResult);
    }

    /**
     * 重置执行进度
     *
     * @param registerId 注册ID
     * @return 重置结果
     */
    @PostMapping("/restExecuteProcess")
    public ResponseResult<?> restExecuteProcess(@RequestBody @Valid Long registerId) {
        asyncDownloadService.resetExecuteProcess(registerId);
        return ResponseResult.ok();
    }

    /**
     * 刷新执行进度
     *
     * @param request 请求
     * @return 刷新结果
     */
    @PostMapping("/refreshExecuteProcess")
    public ResponseResult<?> refreshExecuteProcess(@RequestBody @Valid RefreshExecuteProcessRequest request) {
        asyncDownloadService
            .refreshExecuteProcess(request.getRegisterId(), request.getExecuteProcess(), request.getNextUploadStatus());
        return ResponseResult.ok();
    }

    /**
     * get request info
     *
     * @param registerId registerId
     * @return result
     */
    @PostMapping("/getRequestInfo")
    public ResponseResult<DownloadRequestInfo> getRequestInfo(@RequestBody @Valid @NotNull Long registerId) {
        DownloadRequestInfo requestInfo = asyncDownloadService.getRequestInfoByRegisterId(registerId);
        return ResponseResult.ok(requestInfo);
    }

    /**
     * get app tree
     *
     * @return result
     */
    @PostMapping("/getAppTree")
    public ResponseResult<List<AppTree>> getAppTree() {
        List<AppTree> appTreeList = asyncDownloadService.getAppTree();
        return ResponseResult.ok(appTreeList);
    }

}
