package com.openquartz.easyfile.example.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.storage.download.DownloadStorageService;

/**
 * @author svnee
 **/
@RestController
@RequestMapping("/file-manage")
public class FileManageController {

    @Resource
    private DownloadStorageService downloadStorageService;

    @GetMapping("/get-url")
    public ResponseResult<String> getUrl(@RequestParam("registerId") Long registerId) {
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setDownloadOperateBy("sys");
        downloadRequest.setDownloadOperateName("sys");
        downloadRequest.setRegisterId(registerId);
        DownloadUrlResult result = downloadStorageService.download(downloadRequest);
        return ResponseResult.ok(result.getUrl());
    }

}
