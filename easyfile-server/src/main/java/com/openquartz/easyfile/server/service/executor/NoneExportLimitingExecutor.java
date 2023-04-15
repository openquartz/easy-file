package com.openquartz.easyfile.server.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;

/**
 * @author svnee
 **/
@Slf4j
@Component
public class NoneExportLimitingExecutor implements ExportLimitingExecutor {

    @Override
    public String strategy() {
        return Constants.DEFAULT_LIMITING_STRATEGY;
    }

    @Override
    public void limit(ExportLimitingRequest request) {
        log.debug("[NoneExportLimitingExecutor#limit] strategy:{},request:{}", strategy(), request);
    }
}
