package org.svnee.easyfile.example.controller;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.response.DownloadUrlResult;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * @author svnee
 **/
@RestController
@RequestMapping("/file-manage")
public class FileManageController {

    @Resource
    private DownloadStorageService downloadStorageService;

    @Value("${easyfile.download.app-id}")
    private String appId;

    @GetMapping("/get-url")
    public ResponseResult<String> getUrl(@RequestParam("registerId")Long registerId) {
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setDownloadOperateBy("sys");
        downloadRequest.setDownloadOperateName("sys");
        downloadRequest.setAppId(appId);
        downloadRequest.setRegisterId(registerId);
        DownloadUrlResult result = downloadStorageService.download(downloadRequest);
        return ResponseResult.ok(result.getUrl());
    }

}
