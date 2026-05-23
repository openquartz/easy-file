package com.openquartz.easyfile.admin.controller;

import com.openquartz.easyfile.admin.controller.annotation.Auth;
import com.openquartz.easyfile.admin.model.request.RevokeImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.RetryImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.ShowImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.ViewImportResultRequest;
import com.openquartz.easyfile.admin.model.response.ImportTaskResult;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.service.ServerAppIdProvider;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.PaginationUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * import-task manage
 *
 * @author svnee
 */
@Controller
@RequestMapping("/easyfile-ui/import-task")
@CrossOrigin
@RequiredArgsConstructor
public class ImportTaskController {

    private final ImportStorageService importStorageService;
    private final ServerAppIdProvider serverAppIdProvider;
    private final AdminProperty adminProperty;

    /**
     * 全局-导入记录
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Pagination<ImportTaskResult>> list(
        @RequestBody @Valid ShowImportTaskRequest request
    ) {
        if (StringUtils.isBlank(serverAppIdProvider.getCurrentUnifiedAppId())) {
            return ResponseResult.ok(Pagination.empty());
        }

        ImportResultQueryRequest queryRequest = new ImportResultQueryRequest();
        queryRequest.setAppId(serverAppIdProvider.getCurrentUnifiedAppId());
        queryRequest.setImportStatus(request.getImportStatus());
        queryRequest.setStartTime(request.getStartImportTime());
        queryRequest.setEndTime(request.getEndImportTime());
        queryRequest.setPageNum(request.getPageNum());
        queryRequest.setPageSize(request.getPageSize());

        Pagination<ImportResult> source = importStorageService.listImportResult(queryRequest);
        List<ImportTaskResult> modelList = source.getModelList()
            .stream()
            .map(this::assemble)
            .collect(Collectors.toList());

        return ResponseResult.ok(PaginationUtils.copy(source, modelList));
    }

    private ImportTaskResult assemble(ImportResult importResult) {
        ImportTaskResult target = new ImportTaskResult();
        target.setRegisterId(importResult.getRegisterId());
        target.setImportStatus(importResult.getStatus());
        target.setImportStatusDesc(importResult.getStatus());
        target.setImportCode(StringUtils.EMPTY);
        target.setImportCodeDesc(StringUtils.EMPTY);
        target.setCreateBy(StringUtils.EMPTY);
        target.setCreateTime(importResult.getStartTime());
        target.setLastExecuteTime(importResult.getEndTime());
        target.setExecuteProcess(importResult.getProcess());
        target.setTotalCount(importResult.getTotalCount());
        target.setSuccessCount(importResult.getSuccessCount());
        target.setFailCount(importResult.getFailCount());
        target.setResultFileUrl(importResult.getResultFileUrl());
        target.setErrorMsg(importResult.getErrorMsg());
        target.setAllowRetry("FAILED".equals(importResult.getStatus()));
        target.setAllowRevoke("EXECUTING".equals(importResult.getStatus()));
        return target;
    }

    /**
     * 查看导入结果文件
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/result", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<DownloadUrlResult> result(@RequestBody @Valid ViewImportResultRequest request) {
        return ResponseResult.ok(importStorageService.resultFile(request.getRegisterId()));
    }

    /**
     * 重试导入
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/retry", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> retry(@RequestBody @Valid RetryImportTaskRequest request) {
        return importStorageService.retry(request.getRegisterId(), adminProperty.getAdminUsername())
            ? ResponseResult.ok()
            : ResponseResult.fail("01", "重试失败");
    }

    /**
     * 撤销导入
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> revoke(@RequestBody @Valid RevokeImportTaskRequest request) {
        return importStorageService.revoke(request.getRegisterId(), adminProperty.getAdminUsername())
            ? ResponseResult.ok()
            : ResponseResult.fail("01", "撤销失败");
    }

    /**
     * appId
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/listAppId", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<List<AppTree>> listAppId() {
        return ResponseResult.ok(importStorageService.getAppTree());
    }
}
