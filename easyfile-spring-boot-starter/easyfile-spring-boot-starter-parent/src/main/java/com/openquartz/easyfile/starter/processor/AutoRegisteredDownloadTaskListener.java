package com.openquartz.easyfile.starter.processor;

import com.openquartz.easyfile.common.dictionary.FileHandleTypeEnum;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.core.executor.Executor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.storage.download.DownloadStorageService;

/**
 * 自动注册下载任务监听器
 *
 * @author svnee
 */
@Slf4j
public class AutoRegisteredDownloadTaskListener implements ApplicationListener<ApplicationReadyEvent> {

    private final EasyFileDownloadProperties downloadProperties;
    private final DownloadStorageService storageService;

    public AutoRegisteredDownloadTaskListener(EasyFileDownloadProperties downloadProperties,
                                              DownloadStorageService storageService) {
        this.downloadProperties = downloadProperties;
        this.storageService = storageService;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info(
                ">>>>>>>>>>[EasyFile#AutoRegister]Application:{},UnifiedAppId:{} Ready! auto register async download task!",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());

        List<Executor> executorList = FileExportExecutorSupport.executorList();
        if (CollectionUtils.isEmpty(executorList)) {
            log.info(
                    ">>>>>>>>>>[EasyFile#AutoRegister]Application:{},UnifiedAppId:{} Empty! auto register async download task!",
                    downloadProperties.getAppId(),
                    downloadProperties.getUnifiedAppId());
            return;
        }

        Map<String, String> exportCodeMap = MapUtils.newHashMapWithExpectedSize(executorList.size());
        Map<String, String> importCodeMap = MapUtils.newHashMapWithExpectedSize(executorList.size());

        for (Executor exe : executorList) {
            if (exe instanceof BaseExportExecutor) {
                FileExportExecutor executor = AnnotationUtils
                        .findAnnotation(SpringContextUtil.getRealClass(exe), FileExportExecutor.class);
                assert executor != null;
                exportCodeMap.put(executor.value(), executor.desc());
            }
            if (exe instanceof BaseImportExecutor) {
                FileImportExecutor executor = AnnotationUtils
                        .findAnnotation(SpringContextUtil.getRealClass(exe), FileImportExecutor.class);
                assert executor != null;
                importCodeMap.put(executor.value(), executor.desc());
            }
        }

        doRegisterExport(exportCodeMap);

        doRegisterImport(importCodeMap);
    }

    private void doRegisterImport(Map<String, String> importCodeMap) {

        if (MapUtils.isEmpty(importCodeMap)) {
            return;
        }

        try {
            AutoTaskRegisterRequest registerRequest = new AutoTaskRegisterRequest()
                    .setAppId(downloadProperties.getAppId())
                    .setUnifiedAppId(downloadProperties.getUnifiedAppId())
                    .setHandleType(FileHandleTypeEnum.IMPORT.getCode())
                    .setDownloadCodeMap(importCodeMap);
            storageService.autoRegisterTask(registerRequest);
            log.info(">>>>>>>>>>[EasyFile#AutoRegister] import-executor register success! Application:{},UnifiedAppId:{}",
                    downloadProperties.getAppId(),
                    downloadProperties.getUnifiedAppId());
        } catch (Exception ex) {
            log.warn(">>>>>>>>>>[EasyFile#AutoRegister] import-executor register fail! Application:{},UnifiedAppId:{}",
                    downloadProperties.getAppId(),
                    downloadProperties.getUnifiedAppId());
        }
    }

    private void doRegisterExport(Map<String, String> exportCodeMap) {

        if (MapUtils.isEmpty(exportCodeMap)) {
            return;
        }

        try {
            AutoTaskRegisterRequest registerRequest = new AutoTaskRegisterRequest()
                    .setAppId(downloadProperties.getAppId())
                    .setUnifiedAppId(downloadProperties.getUnifiedAppId())
                    .setHandleType(FileHandleTypeEnum.EXPORT.getCode())
                    .setDownloadCodeMap(exportCodeMap);
            storageService.autoRegisterTask(registerRequest);
            log.info(">>>>>>>>>>[EasyFile#AutoRegister] export-executor register success! Application:{},UnifiedAppId:{}",
                    downloadProperties.getAppId(),
                    downloadProperties.getUnifiedAppId());
        } catch (Exception ex) {
            log.warn(">>>>>>>>>>[EasyFile#AutoRegister] export-executor register fail! Application:{},UnifiedAppId:{}",
                    downloadProperties.getAppId(),
                    downloadProperties.getUnifiedAppId());
        }
    }
}
