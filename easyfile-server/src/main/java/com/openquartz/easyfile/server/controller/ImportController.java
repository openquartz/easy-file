package com.openquartz.easyfile.server.controller;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.common.request.ImportCompleteRequest;
import com.openquartz.easyfile.common.request.ImportOperatorRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.request.ImportUpdateRequest;
import com.openquartz.easyfile.common.request.RefreshImportExecuteProcessRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.server.service.ImportService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导入控制器
 *
 * @author svnee
 */
@RestController
@RequestMapping("/easyfile/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping("/register")
    public ResponseResult<Long> register(@RequestBody @Valid ImportRegisterRequest request) {
        return ResponseResult.ok(importService.registerImport(request));
    }

    @GetMapping("/status/{registerId}")
    public ResponseResult<ImportResult> queryStatus(@PathVariable @NotNull Long registerId) {
        return ResponseResult.ok(importService.queryImportStatus(registerId));
    }

    @PostMapping("/list")
    public ResponseResult<Pagination<ImportResult>> listImportRecords(
        @RequestBody @Valid ImportResultQueryRequest request) {
        return ResponseResult.ok(importService.listImportRecords(request));
    }

    @PostMapping("/retry/{registerId}")
    public ResponseResult<Boolean> retryImportPath(
        @PathVariable @NotNull Long registerId,
        @RequestParam @NotNull String operator) {
        return ResponseResult.ok(importService.retryImport(registerId, operator));
    }

    @PostMapping("/retry")
    public ResponseResult<Boolean> retryImport(@RequestBody @Valid ImportOperatorRequest request) {
        return ResponseResult.ok(importService.retryImport(request.getRegisterId(), request.getOperator()));
    }

    @PostMapping("/revoke/{registerId}")
    public ResponseResult<Boolean> revokeImportPath(
        @PathVariable @NotNull Long registerId,
        @RequestParam @NotNull String operator) {
        return ResponseResult.ok(importService.revokeImport(registerId, operator));
    }

    @PostMapping("/revoke")
    public ResponseResult<Boolean> revokeImport(@RequestBody @Valid ImportOperatorRequest request) {
        return ResponseResult.ok(importService.revokeImport(request.getRegisterId(), request.getOperator()));
    }

    @GetMapping("/result-file/{registerId}")
    public ResponseResult<DownloadUrlResult> downloadResultFile(@PathVariable @NotNull Long registerId) {
        return ResponseResult.ok(importService.getResultFile(registerId));
    }

    @PostMapping("/resultFile")
    public ResponseResult<DownloadUrlResult> resultFile(@RequestBody @Valid @NotNull Long registerId) {
        return ResponseResult.ok(importService.getResultFile(registerId));
    }

    @GetMapping("/app-tree")
    public ResponseResult<List<AppTree>> getAppTree() {
        return ResponseResult.ok(importService.getAppTree());
    }

    @PostMapping("/getAppTree")
    public ResponseResult<List<AppTree>> getAppTreePost() {
        return ResponseResult.ok(importService.getAppTree());
    }

    @PostMapping("/execute/{registerId}")
    public ResponseResult<Boolean> executeImport(@PathVariable @NotNull Long registerId) {
        return ResponseResult.ok(importService.executeImport(registerId));
    }

    @GetMapping("/progress/{registerId}")
    public ResponseResult<ImportResult> queryProgress(@PathVariable @NotNull Long registerId) {
        return ResponseResult.ok(importService.queryImportProgress(registerId));
    }

    @PostMapping("/enableRunning")
    public ResponseResult<Boolean> enableRunning(@RequestBody @Valid EnableRunningRequest request) {
        return ResponseResult.ok(importService.enableRunning(request.getRegisterId()));
    }

    @PostMapping("/getRequestInfo")
    public ResponseResult<ImportRequestInfo> getRequestInfo(@RequestBody @Valid @NotNull Long registerId) {
        return ResponseResult.ok(importService.getRequestInfoByRegisterId(registerId));
    }

    @PostMapping("/refreshExecuteProcess")
    public ResponseResult<?> refreshExecuteProcess(@RequestBody @Valid RefreshImportExecuteProcessRequest request) {
        importService.refreshExecuteProgress(request.getRegisterId(), request.getExecuteProcess(),
            request.getResumePosition(), request.getNextStatus());
        return ResponseResult.ok();
    }

    @PostMapping("/resetExecuteProcess")
    public ResponseResult<?> resetExecuteProcess(@RequestBody @Valid @NotNull Long registerId) {
        importService.resetExecuteProcess(registerId);
        return ResponseResult.ok();
    }

    @PostMapping("/updateTotalCount")
    public ResponseResult<?> updateTotalCount(@RequestBody @Valid ImportUpdateRequest request) {
        importService.updateTotalCount(request.getRegisterId(), request.getTotalCount());
        return ResponseResult.ok();
    }

    @PostMapping("/updateFileHash")
    public ResponseResult<?> updateFileHash(@RequestBody @Valid ImportUpdateRequest request) {
        importService.updateFileHash(request.getRegisterId(), request.getFileHash());
        return ResponseResult.ok();
    }

    @PostMapping("/updateResultFileUrl")
    public ResponseResult<?> updateResultFileUrl(@RequestBody @Valid ImportUpdateRequest request) {
        importService.updateResultFileUrl(request.getRegisterId(), request.getResultFileUrl());
        return ResponseResult.ok();
    }

    @PostMapping("/complete")
    public ResponseResult<?> complete(@RequestBody @Valid ImportCompleteRequest request) {
        importService.complete(request.getRegisterId(), request.getStatus(), request.getTotalCount(),
            request.getSuccessCount(), request.getFailCount(), request.getResumePosition(), request.getErrorMsg());
        return ResponseResult.ok();
    }

    @PostMapping("/cancel")
    public ResponseResult<CancelImportResult> cancelImport(@RequestBody @Valid CancelImportRequest request) {
        return ResponseResult.ok(importService.cancelImport(request));
    }
}
