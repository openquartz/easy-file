package org.svnee.easyfile.storage.impl;

import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.exception.ExpandExecutorErrorCode;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.entity.AsyncDownloadTask;
import org.svnee.easyfile.storage.expand.ExportLimitingExecutor;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;

/**
 * 本地限流服务-限制下载
 *
 * @author svnee
 **/
@Slf4j
public class LocalLimitingServiceImpl implements LimitingService, BeanPostProcessor {

    private final Map<String, ExportLimitingExecutor> limitingExecutorMap = MapUtils.newHashMapWithExpectedSize(10);
    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;

    public LocalLimitingServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper) {
        this.asyncDownloadTaskMapper = asyncDownloadTaskMapper;
    }

    @Override
    public void limiting(ExportLimitingRequest request) {

        AsyncDownloadTask downloadTask = asyncDownloadTaskMapper
            .selectByDownloadCode(request.getDownloadCode(), request.getAppId());
        if (Objects.isNull(downloadTask)) {
            return;
        }

        ExportLimitingExecutor serviceProvider = limitingExecutorMap.get(downloadTask.getLimitingStrategy());
        if (Objects.nonNull(serviceProvider)) {
            serviceProvider.limit(request);
        } else {
            log.error("[limit#downloadCode:{},strategy:{}]没有对应的策略!appId:{},request:{}",
                downloadTask.getLimitingStrategy(), request.getDownloadCode(),
                request.getAppId(), request);
            throw new EasyFileException(ExpandExecutorErrorCode.LIMITING_STRATEGY_EXECUTOR_NOT_EXIST_ERROR,
                downloadTask.getLimitingStrategy());
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof ExportLimitingExecutor) {
            ExportLimitingExecutor executor = (ExportLimitingExecutor) bean;
            ExportLimitingExecutor existExecutor = limitingExecutorMap.putIfAbsent(executor.strategy(), executor);
            Asserts.isTrue(executor == existExecutor,
                ExpandExecutorErrorCode.LIMITING_STRATEGY_EXECUTOR_EXIST_ERROR,
                executor.strategy());
            limitingExecutorMap.put(executor.strategy(), executor);
        }
        return bean;
    }
}
