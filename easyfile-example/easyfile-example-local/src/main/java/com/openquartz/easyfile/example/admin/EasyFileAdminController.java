package com.openquartz.easyfile.example.admin;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;

/**
 * EasyFileAdminController
 *
 * @author svnee
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/easyfile/admin")
public class EasyFileAdminController {

    private final FileTaskStorageService fileTaskStorageService;
    private final EasyFileDownloadProperties easyFileDownloadProperties;

    /**
     * 全局-下载记录
     */
    @PostMapping("/record/global")
    public ResponseResult<Pagination<DownloadResult>> getGlobalRecord(
        @RequestBody @Valid ListDownloadResultRequest request) {
        // 分页结果
        request.setUnifiedAppId(easyFileDownloadProperties.getUnifiedAppId());
        Pagination<DownloadResult> voPagination = fileTaskStorageService.listExportResult(request);
        return ResponseResult.ok(voPagination);
    }

    /**
     * 点击下载
     */
    @PostMapping("/clickDownload")
    public ResponseResult<DownloadUrlResult> clickDownload(@RequestBody @Valid DownloadRequest request) {
        DownloadUrlResult result = fileTaskStorageService.download(request);
        return ResponseResult.ok(result);
    }

    /**
     * 撤销下载
     */
    @PostMapping("/revoke")
    public ResponseResult<?> revoke(@RequestBody @Valid CancelUploadRequest request) {
        fileTaskStorageService.cancelUpload(request);
        return ResponseResult.ok();
    }
}
