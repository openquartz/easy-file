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
import org.springframework.web.servlet.ModelAndView;
import org.svnee.easyfile.admin.controller.annotation.Auth;
import org.svnee.easyfile.admin.model.request.ClickDownloadRequest;
import org.svnee.easyfile.admin.model.request.RevokeDownloadTaskRequest;
import org.svnee.easyfile.admin.model.request.ShowDownloadTaskRequest;
import org.svnee.easyfile.common.response.AppTree;
import org.svnee.easyfile.admin.model.response.DownloadTaskResult;
import org.svnee.easyfile.admin.property.AdminProperty;
import org.svnee.easyfile.admin.service.ServerAppIdProvider;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.DownloadUrlResult;
import org.svnee.easyfile.common.util.PaginationUtils;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * download-task manage
 *
 * @author svnee
 * @since 1.2.0
 */
@Controller
@RequestMapping("/easyfile-ui/download-task")
@CrossOrigin
@RequiredArgsConstructor
public class DownloadTaskController {

    private final DownloadStorageService downloadStorageService;
    private final ServerAppIdProvider serverAppIdProvider;
    private final AdminProperty adminProperty;

    /**
     * get
     */
    @Auth
    @RequestMapping(value = "/get")
    public ModelAndView get() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("download-task");
        modelAndView.addObject("showAppId", serverAppIdProvider.isShow());
        return modelAndView;
    }

    /**
     * 全局-下载记录
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Pagination<DownloadTaskResult>> list(
        @RequestBody ShowDownloadTaskRequest request) {

        ListDownloadResultRequest resultRequest = new ListDownloadResultRequest();
        resultRequest.setUnifiedAppId(serverAppIdProvider.getCurrentUnifiedAppId());
        resultRequest.setStartDownloadTime(request.getStartDownloadTime());
        resultRequest.setEndDownloadTime(request.getEndDownloadTime());
        resultRequest.setPageNum(Objects.nonNull(request.getPageNum()) ? request.getPageNum() : 1);
        resultRequest.setPageSize(Objects.nonNull(request.getPageSize()) ? request.getPageSize() : 10);

        Pagination<DownloadResult> voPagination = downloadStorageService.listExportResult(resultRequest);

        List<DownloadTaskResult> resultList = voPagination.getModelList()
            .stream().map(this::assember)
            .collect(Collectors.toList());

        return ResponseResult.ok(PaginationUtils.copy(voPagination, resultList));
    }

    private DownloadTaskResult assember(DownloadResult downloadResult) {
        DownloadTaskResult downloadTaskResult = new DownloadTaskResult();
        downloadTaskResult.setRegisterId(downloadResult.getRegisterId());
        downloadTaskResult.setUploadStatus(downloadResult.getUploadStatus().getCode());
        downloadTaskResult.setUploadStatusDesc(downloadResult.getUploadStatus().getDesc());
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
    @Auth
    @ResponseBody
    @PostMapping(value = "/download", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<DownloadUrlResult> download(@RequestBody @Valid ClickDownloadRequest request) {

        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setRegisterId(request.getRegisterId());
        downloadRequest.setDownloadOperateBy(adminProperty.getAdminUsername());
        downloadRequest.setDownloadOperateName(adminProperty.getAdminUsername());
        DownloadUrlResult url = downloadStorageService.download(downloadRequest);
        return ResponseResult.ok(url);
    }

    /**
     * 撤销下载
     */
    @Auth
    @ResponseBody
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> revoke(@RequestBody @Valid RevokeDownloadTaskRequest request) {
        CancelUploadRequest cancelRequest = new CancelUploadRequest();
        cancelRequest.setCancelBy(adminProperty.getAdminUsername());
        cancelRequest.setRegisterId(request.getRegisterId());
        CancelUploadResult cancelUpload = downloadStorageService.cancelUpload(cancelRequest);
        if (cancelUpload.isCancelResult()) {
            return ResponseResult.ok();
        } else {
            return ResponseResult.fail("01", cancelUpload.getCancelMsg());
        }
    }


    @Auth
    @ResponseBody
    @PostMapping(value = "/listAppId", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<List<AppTree>> listAppId() {
        List<AppTree> appTreeList = downloadStorageService.getAppTree();
        return ResponseResult.ok(appTreeList);
    }

}
