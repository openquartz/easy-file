package com.openquartz.easyfile.storage.impl;

import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.entity.AsyncDownloadTask;
import com.openquartz.easyfile.storage.expand.ExportLimitingExecutor;
import com.openquartz.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.exception.ExpandExecutorErrorCode;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.util.MapUtils;

/**
 * 本地限流服务-限制下载
 *
 * @author svnee
 **/
@Slf4j
public class LocalLimitingServiceImpl implements LimitingService, BeanPostProcessor {

    private final Map<String, ExportLimitingExecutor> limitingExecutorMap = MapUtils.newHashMapWithExpectedSize(10);
    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;

    public LocalLimitingServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        List<ExportLimitingExecutor> executorList) {
        this.asyncDownloadTaskMapper = asyncDownloadTaskMapper;
        executorList.forEach(executor -> this.limitingExecutorMap.putIfAbsent(executor.strategy(), executor));
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
    public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName)
        throws BeansException {

        if (Objects.nonNull(bean) && bean instanceof ExportLimitingExecutor) {
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
