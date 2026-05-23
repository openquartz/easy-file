package com.openquartz.easyfile.server.controller;

import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.server.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 导入控制器
 * 提供导入任务管理的RESTful API接口
 *
 * @author svnee
 */
@RestController
@RequestMapping("/easyfile/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    /**
     * 注册导入任务
     *
     * @param request 导入注册请求
     * @return 导入任务注册ID
     */
    @PostMapping("/register")
    public ResponseResult<Long> register(@RequestBody @Valid ImportRegisterRequest request) {
        Long registerId = importService.registerImport(request);
        return ResponseResult.ok(registerId);
    }

    /**
     * 查询导入状态
     *
     * @param registerId 注册ID
     * @return 导入状态信息
     */
    @GetMapping("/status/{registerId}")
    public ResponseResult<ImportResult> queryStatus(@PathVariable @NotNull Long registerId) {
        ImportResult status = importService.queryImportStatus(registerId);
        return ResponseResult.ok(status);
    }

    /**
     * 分页查询导入记录
     *
     * @param request 查询请求
     * @return 导入记录分页结果
     */
    @PostMapping("/list")
    public ResponseResult<Pagination<ImportResult>> listImportRecords(
        @RequestBody @Valid ImportResultQueryRequest request) {
        Pagination<ImportResult> resultList = importService.listImportRecords(request);
        return ResponseResult.ok(resultList);
    }

    /**
     * 重试失败的导入任务
     *
     * @param registerId 注册ID
     * @param operator 操作人
     * @return 重试结果
     */
    @PostMapping("/retry/{registerId}")
    public ResponseResult<Boolean> retryImport(
        @PathVariable @NotNull Long registerId,
        @RequestParam @NotNull String operator) {
        boolean result = importService.retryImport(registerId, operator);
        return ResponseResult.ok(result);
    }

    /**
     * 撤销导入任务
     *
     * @param registerId 注册ID
     * @param operator 操作人
     * @return 撤销结果
     */
    @PostMapping("/revoke/{registerId}")
    public ResponseResult<Boolean> revokeImport(
        @PathVariable @NotNull Long registerId,
        @RequestParam @NotNull String operator) {
        boolean result = importService.revokeImport(registerId, operator);
        return ResponseResult.ok(result);
    }

    /**
     * 下载导入结果文件
     *
     * @param registerId 注册ID
     * @return 结果文件下载信息
     */
    @GetMapping("/result-file/{registerId}")
    public ResponseResult<DownloadUrlResult> downloadResultFile(
        @PathVariable @NotNull Long registerId) {
        DownloadUrlResult result = importService.getResultFile(registerId);
        return ResponseResult.ok(result);
    }

    /**
     * 获取应用树结构
     *
     * @return 应用树列表
     */
    @GetMapping("/app-tree")
    public ResponseResult<java.util.List<AppTree>> getAppTree() {
        java.util.List<AppTree> appTreeList = importService.getAppTree();
        return ResponseResult.ok(appTreeList);
    }

    /**
     * 手动触发导入执行
     *
     * @param registerId 注册ID
     * @return 执行结果
     */
    @PostMapping("/execute/{registerId}")
    public ResponseResult<Boolean> executeImport(@PathVariable @NotNull Long registerId) {
        boolean result = importService.executeImport(registerId);
        return ResponseResult.ok(result);
    }

    /**
     * 查询导入进度
     *
     * @param registerId 注册ID
     * @return 导入进度信息
     */
    @GetMapping("/progress/{registerId}")
    public ResponseResult<ImportResult> queryProgress(@PathVariable @NotNull Long registerId) {
        ImportResult progress = importService.queryImportProgress(registerId);
        return ResponseResult.ok(progress);
    }
}
