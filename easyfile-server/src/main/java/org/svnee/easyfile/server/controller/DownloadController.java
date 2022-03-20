package org.svnee.easyfile.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.ResponseBaseVo;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.EnableRunningRequest;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.server.service.AsyncDownloadService;

/**
 * 下载controller
 *
 * @author svnee
 */
@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
public class DownloadController {

    private final AsyncDownloadService asyncDownloadService;

    /**
     * 限流
     */
    @PostMapping("/limiting")
    public ResponseBaseVo<?> exportLimiting(@RequestBody @Valid ExportLimitingRequest request) {
        boolean limitResult = asyncDownloadService.limiting(request);
        if (limitResult) {
            return ResponseBaseVo.ok();
        } else {
            return ResponseBaseVo.fail("01", "超过限流标准");
        }
    }

    /**
     * 注册下载任务
     *
     * @param request 注册下载请求
     * @return 返回下载记录ID
     */
    @PostMapping("/register")
    public ResponseBaseVo<Long> register(@RequestBody @Valid RegisterDownloadRequest request) {
        Long registerId = asyncDownloadService.register(request);
        return ResponseBaseVo.ok(registerId);
    }

    /**
     * 下载任务回传
     *
     * @param request 上传文件回传结果
     * @return 上传
     */
    @PostMapping("/callback")
    public ResponseBaseVo<?> uploadCallback(@RequestBody @Valid UploadCallbackRequest request) {
        asyncDownloadService.uploadCallback(request);
        return ResponseBaseVo.ok();
    }

    /**
     * 用户撤销任务上传
     *
     * @param request 请求
     * @return 请求
     */
    @PostMapping("/cancel")
    public ResponseBaseVo<CancelUploadResult> cancelUpload(@RequestBody @Valid CancelUploadRequest request) {
        CancelUploadResult result = asyncDownloadService.cancel(request);
        return ResponseBaseVo.ok(result);
    }

    /**
     * 校验是否可运行
     *
     * @param request 校验运行请求
     * @return 运行请求
     */
    @PostMapping("/enableRunning")
    public ResponseBaseVo<Boolean> enableRunning(@RequestBody @Valid EnableRunningRequest request) {
        boolean running = asyncDownloadService.enableRunning(request.getRegisterId());
        return ResponseBaseVo.ok(running);
    }

    /**
     * 导出结果查询
     *
     * @param request 查询请求
     * @return 导出结果
     */
    @PostMapping("/listExport")
    public ResponseBaseVo<Pagination<DownloadResult>> listExportResult(
        @RequestBody @Valid ListDownloadResultRequest request) {
        Pagination<DownloadResult> resultList = asyncDownloadService.listExportResult(request);
        return ResponseBaseVo.ok(resultList);
    }

    /**
     * 下载次数
     *
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/file")
    public ResponseBaseVo<?> download(@RequestBody @Valid DownloadRequest request) {
        asyncDownloadService.download(request);
        return ResponseBaseVo.ok();
    }

}
