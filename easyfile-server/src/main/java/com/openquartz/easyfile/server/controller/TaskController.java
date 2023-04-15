package com.openquartz.easyfile.server.controller;

import com.openquartz.easyfile.server.service.AsyncDownloadTaskService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;

/**
 * TaskController
 *
 * @author svnee
 */
@RestController
@RequestMapping("/easyfile/task")
@RequiredArgsConstructor
public class TaskController {

    private final AsyncDownloadTaskService asyncDownloadTaskService;

    /**
     * 自动注册
     *
     * @param request 注册请求
     * @return 结果
     */
    @PostMapping("/autoRegister")
    public ResponseResult<Void> autoRegister(@RequestBody @Valid AutoTaskRegisterRequest request) {
        asyncDownloadTaskService.autoRegister(request);
        return ResponseResult.ok();
    }

}
