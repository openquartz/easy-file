package org.svnee.easyfile.server.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.ResponseBaseVo;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.server.service.AsyncDownloadTaskService;

/**
 * TaskController
 *
 * @author xuzhao
 */
@RestController
@RequestMapping("/task")
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
    public ResponseBaseVo<?> autoRegister(@RequestBody @Valid AutoTaskRegisterRequest request) {
        asyncDownloadTaskService.autoRegister(request);
        return ResponseBaseVo.ok();
    }

}
