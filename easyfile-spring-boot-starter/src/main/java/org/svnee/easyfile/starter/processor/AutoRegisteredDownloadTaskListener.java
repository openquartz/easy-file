package org.svnee.easyfile.starter.processor;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.svnee.easyfile.core.annotations.FileExportExecutor;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.core.executor.BaseDownloadExecutor;
import org.svnee.easyfile.core.executor.support.FileExportExecutorSupport;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;

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

        List<BaseDownloadExecutor> executorList = FileExportExecutorSupport.executorList();
        if (CollectionUtils.isEmpty(executorList)) {
            log.info(
                ">>>>>>>>>>[EasyFile#AutoRegister]Application:{},UnifiedAppId:{} Empty! auto register async download task!",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());
            return;
        }
        Map<String, String> downloadCodeMap = MapUtils.newHashMapWithExpectedSize(executorList.size());
        for (BaseDownloadExecutor exe : executorList) {
            FileExportExecutor executor = AnnotationUtils
                .findAnnotation(SpringContextUtil.getRealClass(exe), FileExportExecutor.class);
            assert executor != null;
            downloadCodeMap.put(executor.value(), executor.desc());
        }
        try {
            AutoTaskRegisterRequest registerRequest = new AutoTaskRegisterRequest()
                .setAppId(downloadProperties.getAppId())
                .setUnifiedAppId(downloadProperties.getUnifiedAppId())
                .setDownloadCodeMap(downloadCodeMap);
            storageService.autoRegisterTask(registerRequest);
            log.info(">>>>>>>>>>[EasyFile#AutoRegister] register success! Application:{},UnifiedAppId:{}",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());
        } catch (Exception ex) {
            log.warn(">>>>>>>>>>[EasyFile#AutoRegister] register fail! Application:{},UnifiedAppId:{}",
                downloadProperties.getAppId(),
                downloadProperties.getUnifiedAppId());
        }
    }
}
