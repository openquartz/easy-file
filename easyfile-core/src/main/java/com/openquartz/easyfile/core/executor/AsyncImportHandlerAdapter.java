package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步导入处理器适配器
 * 提供 enableRunning / 幂等短路 / process / callback 默认编排
 *
 * @author svnee
 */
public abstract class AsyncImportHandlerAdapter implements BaseAsyncImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(AsyncImportHandlerAdapter.class);

    private final ImportStorageService importStorageService;
    private final IdempotentImportManager idempotentImportManager;

    protected AsyncImportHandlerAdapter(ImportStorageService importStorageService) {
        this(importStorageService, null);
    }

    protected AsyncImportHandlerAdapter(ImportStorageService importStorageService,
        IdempotentImportManager idempotentImportManager) {
        this.importStorageService = importStorageService;
        this.idempotentImportManager = idempotentImportManager;
    }

    protected ImportStorageService getImportStorageService() {
        return importStorageService;
    }

    @Override
    public boolean handle(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        ImportProcessResult result = handleResult(executor, context, registerId);
        return result != null && result.isSuccess();
    }

    @Override
    public ImportProcessResult handleResult(BaseImportExecutor<?> executor, ImportRequestContext context,
        Long registerId) {
        ImportContext importContext = buildImportContext(context, registerId);

        // Idempotent short-circuit before enableRunning (mirror download cache-before-enableRunning)
        ImportProcessResult idempotentResult = tryIdempotentShortCircuit(importContext);
        if (idempotentResult != null) {
            executor.asyncCompleteCallback(idempotentResult, context);
            return idempotentResult;
        }

        if (!importStorageService.enableRunning(registerId)) {
            logger.error("[AsyncImportHandlerAdapter#handleResult] registerId has running!skip,registerId:{}",
                registerId);
            return buildRejectResult(registerId);
        }

        ImportProcessResult result;
        try {
            result = executor.process(importContext);
        } catch (Exception ex) {
            logger.error("[AsyncImportHandlerAdapter#handleResult] process error!registerId:{}", registerId, ex);
            completeAsFail(registerId, ex.getMessage());
            result = buildFailResult(registerId, ex.getMessage());
            executor.asyncCompleteCallback(result, context);
            return result;
        }

        if (result == null) {
            result = buildFailResult(registerId, "import process returned null");
            completeAsFail(registerId, result.getErrorMsg());
        }

        executor.asyncCompleteCallback(result, context);
        return result;
    }

    /**
     * After enableRunning (NONE→EXECUTING), failure exits that skip process's own complete must finish storage.
     */
    private void completeAsFail(Long registerId, String errorMsg) {
        importStorageService.complete(registerId, ImportStatusEnum.FAIL, 0L, 0L, 0L, 0L, errorMsg);
    }

    /**
     * 执行导入（含日志），供线程池/DB 调度调用
     */
    public void doExecute(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        logger.info("[AsyncImportHandlerAdapter#doExecute] start,registerId:{}", registerId);
        try {
            handleResult(executor, context, registerId);
        } catch (Exception ex) {
            logger.error("[AsyncImportHandlerAdapter#doExecute] error!registerId:{}", registerId, ex);
            throw ex;
        }
        logger.info("[AsyncImportHandlerAdapter#doExecute] end,registerId:{}", registerId);
    }

    private ImportProcessResult tryIdempotentShortCircuit(ImportContext importContext) {
        if (idempotentImportManager == null || StringUtils.isBlank(importContext.getBusinessUniqueKey())) {
            return null;
        }
        if (!idempotentImportManager.isExecuted(importContext.getBusinessUniqueKey())) {
            return null;
        }
        ImportResult existed = idempotentImportManager.getExecutedResult(importContext.getBusinessUniqueKey());
        ImportProcessResult result = ImportProcessResult.builder().build();
        result.setRegisterId(importContext.getRegisterId());
        result.setIdempotent(true);
        result.setComplete(true);
        result.setStartTime(new Date());
        result.setEndTime(new Date());
        result.setErrorRecords(Collections.emptyList());
        if (existed == null) {
            result.setSuccess(false);
            result.setComplete(false);
            result.setErrorMsg("幂等结果不存在，请重试");
            return result;
        }
        result.setSuccess(Boolean.TRUE.equals(existed.isSuccess())
            || "SUCCESS".equals(existed.getStatus())
            || "PARTIAL_SUCCESS".equals(existed.getStatus()));
        result.setTotalCount(existed.getTotalCount() == null ? 0L : existed.getTotalCount());
        result.setSuccessCount(existed.getSuccessCount() == null ? 0 : existed.getSuccessCount().intValue());
        result.setFailCount(existed.getFailCount() == null ? 0 : existed.getFailCount().intValue());
        result.setErrorMsg(existed.getErrorMsg());
        return result;
    }

    protected ImportContext buildImportContext(ImportRequestContext context, Long registerId) {
        ImportRequestInfo info = importStorageService.getRequestInfoByRegisterId(registerId);
        if (info != null && info.getRegisterRequest() != null) {
            ImportRegisterRequest req = info.getRegisterRequest();
            return ImportContext.builder()
                .registerId(registerId)
                .appId(req.getAppId())
                .importCode(StringUtils.isNotBlank(info.getImportCode()) ? info.getImportCode() : req.getImportCode())
                .ossUrl(req.getOssUrl())
                .fileName(req.getFileName())
                .fileType(req.getFileType())
                .businessUniqueKey(req.getBusinessUniqueKey())
                .executeParam(req.getExecuteParam())
                .resumePosition(info.getResumePosition() == null ? 0L : info.getResumePosition())
                .maxRetry(req.getMaxRetry())
                .enableNotify(req.getEnableNotify())
                .createBy(req.getCreateBy())
                .build();
        }

        ImportRequestContext safeContext = Objects.nonNull(context) ? context : new ImportRequestContext();
        return ImportContext.builder()
            .registerId(registerId)
            .importCode(safeContext.getImportCode())
            .ossUrl(safeContext.getSourceFileUrl())
            .fileName(safeContext.getFileName())
            .fileType(safeContext.getFileType())
            .businessUniqueKey(safeContext.getBusinessUniqueKey())
            .executeParam(safeContext.getExecuteParam())
            .createBy(safeContext.getOperator())
            .build();
    }

    private ImportProcessResult buildRejectResult(Long registerId) {
        ImportProcessResult result = ImportProcessResult.builder().build();
        result.setRegisterId(registerId);
        result.setSuccess(false);
        result.setComplete(false);
        result.setErrorMsg("任务正在执行或不可运行");
        result.setErrorRecords(Collections.emptyList());
        return result;
    }

    private ImportProcessResult buildFailResult(Long registerId, String errorMsg) {
        ImportProcessResult result = ImportProcessResult.builder().build();
        result.setRegisterId(registerId);
        result.setSuccess(false);
        result.setComplete(false);
        result.setErrorMsg(errorMsg);
        result.setErrorRecords(Collections.emptyList());
        return result;
    }
}
