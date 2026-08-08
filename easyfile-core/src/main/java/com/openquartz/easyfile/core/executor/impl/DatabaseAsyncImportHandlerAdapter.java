package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.executor.AsyncImportHandlerAdapter;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.core.executor.support.FileImportExecutorSupport;
import com.openquartz.easyfile.core.executor.support.FileImportTriggerContext;
import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.imports.ImportTriggerService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * DB 调度异步导入处理器
 *
 * @author svnee
 */
@Slf4j
public abstract class DatabaseAsyncImportHandlerAdapter extends AsyncImportHandlerAdapter implements InitializingBean {

    private final ImportTriggerService triggerService;
    private final ImportStorageService storageService;
    private final IDatabaseAsyncHandlerProperty handlerProperties;

    private final ScheduledThreadPoolExecutor compensateScheduleExecutorService = new ScheduledThreadPoolExecutor(1,
        new ThreadFactoryBuilder()
            .setNameFormat("CompensateScheduleAsyncImportHandler-thread-%d")
            .build());

    protected DatabaseAsyncImportHandlerAdapter(
        ImportStorageService storageService,
        ImportTriggerService triggerService,
        IDatabaseAsyncHandlerProperty handlerProperties) {
        this(storageService, triggerService, handlerProperties, null);
    }

    protected DatabaseAsyncImportHandlerAdapter(
        ImportStorageService storageService,
        ImportTriggerService triggerService,
        IDatabaseAsyncHandlerProperty handlerProperties,
        IdempotentImportManager idempotentImportManager) {
        super(storageService, idempotentImportManager);
        this.triggerService = triggerService;
        this.handlerProperties = handlerProperties;
        this.storageService = storageService;
    }

    @Override
    public void execute(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        ImportTriggerRequest triggerRequest = new ImportTriggerRequest();
        triggerRequest.setRegisterId(registerId);
        triggerService.trigger(triggerRequest);
    }

    /**
     * 做补偿
     */
    public void doCompensate() {
        log.info("[DatabaseAsyncImportHandlerAdapter#doCompensate] start...");
        try {
            triggerService.handleExpirationTrigger(handlerProperties.getMaxExecuteTimeout());
            triggerService.archiveHistoryTrigger(handlerProperties.getMaxArchiveHours(),
                handlerProperties.getMaxTriggerCount());
        } catch (Exception ex) {
            log.error("[DatabaseAsyncImportHandlerAdapter#doCompensate] error!...", ex);
        }
        log.info("[DatabaseAsyncImportHandlerAdapter#doCompensate] end...");
    }

    public void doTrigger(ImportTriggerEntry entry) {
        boolean execute = triggerService.startExecute(entry.getRegisterId(), entry.getTriggerCount());
        if (!execute) {
            return;
        }
        try {
            ImportRequestInfo requestInfo = storageService.getRequestInfoByRegisterId(entry.getRegisterId());
            BaseImportExecutor<?> executor = FileImportExecutorSupport.get(requestInfo.getImportCode());
            ImportRequestContext context = toRequestContext(requestInfo, entry.getRegisterId());

            FileImportTriggerContext.setAsyncTriggerFlagIfAbsent(true);
            doExecute(executor, context, entry.getRegisterId());
            triggerService.exeSuccess(entry.getRegisterId());
        } catch (Exception ex) {
            log.error("[DatabaseAsyncImportHandlerAdapter#doTrigger] execute-failed!registerId:{}",
                entry.getRegisterId(), ex);
            triggerService.exeFail(entry.getRegisterId());
        } finally {
            FileImportTriggerContext.clear();
        }
    }

    private ImportRequestContext toRequestContext(ImportRequestInfo requestInfo, Long registerId) {
        ImportRequestContext context = new ImportRequestContext();
        context.setRegisterId(registerId);
        if (requestInfo == null) {
            return context;
        }
        context.setImportCode(requestInfo.getImportCode());
        ImportRegisterRequest registerRequest = requestInfo.getRegisterRequest();
        if (registerRequest != null) {
            context.setSourceFileUrl(registerRequest.getOssUrl());
            context.setFileName(registerRequest.getFileName());
            context.setFileType(registerRequest.getFileType());
            context.setBusinessUniqueKey(registerRequest.getBusinessUniqueKey());
            context.setExecuteParam(registerRequest.getExecuteParam());
            context.setNotifyEmail(registerRequest.getNotifyEmail());
            context.setLocale(registerRequest.getLocale());
            context.setOperator(registerRequest.getCreateBy());
            if (context.getImportCode() == null) {
                context.setImportCode(registerRequest.getImportCode());
            }
        }
        return context;
    }

    @Override
    public void afterPropertiesSet() {
        compensateScheduleExecutorService.scheduleAtFixedRate(this::doCompensate,
            handlerProperties.getSchedulePeriod(),
            handlerProperties.getMaxExecuteTimeout(), TimeUnit.SECONDS);
    }
}
