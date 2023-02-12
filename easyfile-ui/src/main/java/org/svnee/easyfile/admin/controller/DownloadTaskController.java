package org.svnee.easyfile.admin.controller;

import java.util.List;
import java.util.Objects;
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
import org.svnee.easyfile.admin.model.request.ShowDownloadTaskRequest;
import org.svnee.easyfile.admin.model.response.DownloadTaskResult;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.property.IEasyFileCommonProperties;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.util.PaginationUtils;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * download-task manage
 *
 * @author svnee
 */
@Controller
@RequestMapping("/easyfile-ui/download-task")
@CrossOrigin
@RequiredArgsConstructor
public class DownloadTaskController {

    private final DownloadStorageService downloadStorageService;
    private final IEasyFileCommonProperties easyFileDownloadProperties;

    /**
     * get
     */
    @RequestMapping(value = "/get")
    public String get() {
        return "download-task";
    }

    /**
     * 全局-下载记录
     */
    @ResponseBody
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Pagination<DownloadTaskResult>> list(
        @RequestBody @Valid ShowDownloadTaskRequest request) {

        ListDownloadResultRequest resultRequest = new ListDownloadResultRequest();
        resultRequest.setUnifiedAppId(easyFileDownloadProperties.getUnifiedAppId());
        resultRequest.setStartDownloadTime(request.getStartDownloadTime());
        resultRequest.setEndDownloadTime(request.getEndDownloadTime());
        resultRequest.setPageNum(Objects.nonNull(request.getPageNum()) ? request.getPageNum() : 1);
        resultRequest.setPageSize(Objects.nonNull(request.getPageSize()) ? request.getPageSize() : 10);

        Pagination<DownloadResult> voPagination = downloadStorageService.listExportResult(resultRequest);

        List<DownloadTaskResult> resultList = voPagination.getModelList().stream().map(this::assember)
            .collect(Collectors.toList());

        return ResponseResult.ok(PaginationUtils.copy(voPagination,resultList));
    }

    private DownloadTaskResult assember(DownloadResult downloadResult) {
        DownloadTaskResult downloadTaskResult = new DownloadTaskResult();
        downloadTaskResult.setRegisterId(downloadResult.getRegisterId());
        downloadTaskResult.setUploadStatus(downloadResult.getUploadStatus().getCode());
        downloadTaskResult.setUploadStatusDesc(downloadResult.getUploadStatus().getDesc());
        downloadTaskResult.setFileSystem(downloadResult.getFileSystem());
        downloadTaskResult.setFileUrl(downloadResult.getFileUrl());
        downloadTaskResult.setErrorMsg(downloadResult.getErrorMsg());
        downloadTaskResult.setDownloadCode(downloadResult.getDownloadCode());
        downloadTaskResult.setDownloadCodeDesc(downloadResult.getDownloadCodeDesc());
        downloadTaskResult.setDownloadOperateBy(downloadResult.getDownloadOperateBy());
        downloadTaskResult.setDownloadOperateName(downloadResult.getDownloadOperateName());
        downloadTaskResult.setExportTime(downloadResult.getExportTime());
        downloadTaskResult.setLastExecuteTime(downloadResult.getLastExecuteTime());
        downloadTaskResult.setInvalidTime(downloadResult.getInvalidTime());
        downloadTaskResult.setDownloadNum(downloadResult.getDownloadNum());
        downloadTaskResult.setExecuteProcess(downloadResult.getExecuteProcess());
        downloadTaskResult.setUpdateBy(downloadResult.getUpdateBy());
        return downloadTaskResult;
    }

    /**
     * 点击下载
     */
    @ResponseBody
    @PostMapping(value = "/download", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<String> download(@RequestBody @Valid DownloadRequest request) {
        String url = downloadStorageService.download(request);
        return ResponseResult.ok(url);
    }

    /**
     * 撤销下载
     */
    @ResponseBody
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> revoke(@RequestBody @Valid CancelUploadRequest request) {
        downloadStorageService.cancelUpload(request);
        return ResponseResult.ok();
    }

}
