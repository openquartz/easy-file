package com.openquartz.easyfile.server.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.constants.Constants;

/**
 * Health check controller.
 *
 * @author svnee
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/health/check")
public class HealthCheckController {

    @GetMapping
    public ResponseResult<String> healthCheck() {
        return ResponseResult.ok(Constants.UP);
    }

}
