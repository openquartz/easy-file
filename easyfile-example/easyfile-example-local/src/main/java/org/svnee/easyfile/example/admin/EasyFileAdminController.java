package org.svnee.easyfile.example.admin;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * EasyFileAdminController
 *
 * @author svnee
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/easyfile/admin")
public class EasyFileAdminController {

    private final DownloadStorageService downloadStorageService;
    private final EasyFileDownloadProperties easyFileDownloadProperties;

    /**
     * 全局-下载记录
     */
    @PostMapping("/record/global")
    public ResponseResult<Pagination<DownloadResult>> getGlobalRecord(
        @RequestBody @Valid ListDownloadResultRequest request) {
        // 分页结果
        request.setUnifiedAppId(easyFileDownloadProperties.getUnifiedAppId());
        Pagination<DownloadResult> voPagination = downloadStorageService.listExportResult(request);
        return ResponseResult.ok(voPagination);
    }

    /**
     * 点击下载
     */
    @PostMapping("/clickDownload")
    public ResponseResult<String> clickDownload(@RequestBody @Valid DownloadRequest request) {
        String url = downloadStorageService.download(request);
        return ResponseResult.ok(url);
    }

    /**
     * 撤销下载
     */
    @PostMapping("/revoke")
    public ResponseResult<?> revoke(@RequestBody @Valid CancelUploadRequest request) {
        downloadStorageService.cancelUpload(request);
        return ResponseResult.ok();
    }
}
