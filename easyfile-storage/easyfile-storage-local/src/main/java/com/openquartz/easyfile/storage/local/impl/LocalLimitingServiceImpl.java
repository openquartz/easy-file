package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.local.entity.AsyncDownloadTask;
import com.openquartz.easyfile.storage.expand.ExportLimitingExecutor;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.ImportRecordQueryCondition;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.exception.ExpandExecutorErrorCode;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.common.util.StringUtils;

/**
 * 本地限流服务 — 导出策略执行 + 导入并发记录数限制
 *
 * @author svnee
 **/
@Slf4j
public class LocalLimitingServiceImpl implements LimitingService, BeanPostProcessor {

    /**
     * Default max in-flight import records (NONE + EXECUTING) per appId+importCode.
     */
    public static final int DEFAULT_MAX_CONCURRENT_IMPORT_TASKS = 10;

    private final Map<String, ExportLimitingExecutor> limitingExecutorMap = MapUtils.newHashMapWithExpectedSize(10);
    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    private final AsyncImportRecordMapper asyncImportRecordMapper;
    private final int maxConcurrentImportTasks;

    public LocalLimitingServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        List<ExportLimitingExecutor> executorList) {
        this(asyncDownloadTaskMapper, executorList, null, DEFAULT_MAX_CONCURRENT_IMPORT_TASKS);
    }

    public LocalLimitingServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        List<ExportLimitingExecutor> executorList,
        AsyncImportRecordMapper asyncImportRecordMapper) {
        this(asyncDownloadTaskMapper, executorList, asyncImportRecordMapper, DEFAULT_MAX_CONCURRENT_IMPORT_TASKS);
    }

    public LocalLimitingServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        List<ExportLimitingExecutor> executorList,
        AsyncImportRecordMapper asyncImportRecordMapper,
        int maxConcurrentImportTasks) {
        this.asyncDownloadTaskMapper = asyncDownloadTaskMapper;
        this.asyncImportRecordMapper = asyncImportRecordMapper;
        this.maxConcurrentImportTasks = maxConcurrentImportTasks > 0
            ? maxConcurrentImportTasks : DEFAULT_MAX_CONCURRENT_IMPORT_TASKS;
        if (executorList != null) {
            executorList.forEach(executor -> this.limitingExecutorMap.putIfAbsent(executor.strategy(), executor));
        }
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
    public void limiting(ImportLimitingRequest request) {
        if (request == null || asyncImportRecordMapper == null) {
            return;
        }
        if (StringUtils.isBlank(request.getAppId()) || StringUtils.isBlank(request.getImportCode())) {
            return;
        }
        int inFlight = countInFlightImports(request.getAppId(), request.getImportCode());
        if (inFlight >= maxConcurrentImportTasks) {
            log.warn("[import-limit] rejected appId={}, importCode={}, inFlight={}, max={}",
                request.getAppId(), request.getImportCode(), inFlight, maxConcurrentImportTasks);
            throw EasyFileException.replacePlaceHold(
                ExpandExecutorErrorCode.IMPORT_LIMITING_EXCEEDED_ERROR,
                request.getAppId(), request.getImportCode());
        }
    }

    private int countInFlightImports(String appId, String importCode) {
        int total = 0;
        for (ImportStatusEnum status : new ImportStatusEnum[]{ImportStatusEnum.NONE, ImportStatusEnum.EXECUTING}) {
            ImportRecordQueryCondition condition = new ImportRecordQueryCondition();
            condition.setLimitedAppIdList(Collections.singletonList(appId));
            condition.setImportCode(importCode);
            condition.setImportStatus(status);
            total += asyncImportRecordMapper.countByCondition(condition);
        }
        return total;
    }

    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName)
        throws BeansException {

        if (Objects.nonNull(bean) && bean instanceof ExportLimitingExecutor) {
            ExportLimitingExecutor executor = (ExportLimitingExecutor) bean;
            ExportLimitingExecutor existExecutor = limitingExecutorMap.putIfAbsent(executor.strategy(), executor);
            Asserts.isTrue(existExecutor == null || executor == existExecutor,
                ExpandExecutorErrorCode.LIMITING_STRATEGY_EXECUTOR_EXIST_ERROR,
                executor.strategy());
        }
        return bean;
    }
}
