package com.openquartz.easyfile.server.controller;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.RegisterImportRequest;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导入 Controller
 *
 * @author svnee
 */
@RestController
@RequestMapping("/easyfile/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportStorageService importStorageService;

    /**
     * 创建导入任务
     *
     * @param request 注册导入请求
     * @return 注册ID
     */
    @PostMapping("/create")
    public ResponseResult<Long> create(@RequestBody @Valid RegisterImportRequest request) {
        Long registerId = importStorageService.register(request);
        return ResponseResult.ok(registerId);
    }
}
