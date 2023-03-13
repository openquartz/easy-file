package org.svnee.easyfile.example.admin;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.util.page.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.DownloadUrlResult;
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
    public ResponseResult<DownloadUrlResult> clickDownload(@RequestBody @Valid DownloadRequest request) {
        DownloadUrlResult result = downloadStorageService.download(request);
        return ResponseResult.ok(result);
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
