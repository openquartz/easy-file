package com.openquartz.easyfile.starter.processor;

import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.core.executor.support.FileImportExecutorSupport;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * 自动注册导入任务监听器
 *
 * @author svnee
 */
@Slf4j
public class AutoRegisteredImportTaskListener implements ApplicationListener<ApplicationReadyEvent> {

    private final EasyFileDownloadProperties downloadProperties;
    private final ImportStorageService importStorageService;

    public AutoRegisteredImportTaskListener(
        EasyFileDownloadProperties downloadProperties,
        ImportStorageService importStorageService
    ) {
        this.downloadProperties = downloadProperties;
        this.importStorageService = importStorageService;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info(
            ">>>>>>>>>>[EasyFile#AutoRegister]Application:{},UnifiedAppId:{} Ready! auto register async import task!",
            downloadProperties.getAppId(),
            downloadProperties.getUnifiedAppId());

        Map<String, String> importCodeMap = MapUtils.newHashMap();
        importCodeMap.putAll(FileImportExecutorSupport.getImportCodeMap());
        if (MapUtils.isEmpty(importCodeMap)) {
            log.info(
                ">>>>>>>>>>[EasyFile#AutoRegister]Application:{},UnifiedAppId:{} Empty! auto register async import task!",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());
            return;
        }
        try {
            AutoTaskRegisterRequest registerRequest = new AutoTaskRegisterRequest()
                .setAppId(downloadProperties.getAppId())
                .setUnifiedAppId(downloadProperties.getUnifiedAppId())
                .setImportCodeMap(importCodeMap);
            importStorageService.autoRegisterTask(registerRequest);
            log.info(">>>>>>>>>>[EasyFile#AutoRegister] register success! Application:{},UnifiedAppId:{}",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());
        } catch (Exception ex) {
            log.warn(">>>>>>>>>>[EasyFile#AutoRegister] register fail! Application:{},UnifiedAppId:{}",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId(), ex);
        }
    }
}
