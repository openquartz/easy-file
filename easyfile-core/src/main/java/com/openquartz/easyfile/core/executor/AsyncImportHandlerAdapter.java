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

        // Idempotent short-circuit before enableRunning: still complete *this* registerId (avoid NONE forever)
        ImportProcessResult idempotentResult = tryIdempotentShortCircuit(importContext);
        if (idempotentResult != null) {
            completeIdempotentStorage(registerId, idempotentResult);
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
        // Idempotent short-circuit inside process completes storage in process finally;
        // pre-enableRunning short-circuit is completed above.

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
     * Copy prior idempotent outcome onto the current registerId so it does not stick in NONE/EXECUTING.
     */
    private void completeIdempotentStorage(Long registerId, ImportProcessResult result) {
        ImportStatusEnum status;
        if (result.isComplete() && result.isSuccess()) {
            status = result.getFailCount() > 0 ? ImportStatusEnum.PARTIAL_SUCCESS : ImportStatusEnum.SUCCESS;
        } else if (result.isComplete() && !result.isSuccess() && result.getFailCount() > 0) {
            status = ImportStatusEnum.PARTIAL_SUCCESS;
        } else if (!result.isComplete()) {
            status = ImportStatusEnum.FAIL;
        } else {
            status = result.isSuccess() ? ImportStatusEnum.SUCCESS : ImportStatusEnum.FAIL;
        }
        importStorageService.complete(
            registerId,
            status,
            result.getTotalCount(),
            (long) result.getSuccessCount(),
            (long) result.getFailCount(),
            result.getTotalCount(),
            result.getErrorMsg());
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
        // Same businessUniqueKey + different fileHash ⇒ reject on short-circuit path.
        // If prior hash is known but current hash is not yet available, skip short-circuit so
        // enableRunning+process can run the executor hash gate after download/MD5.
        String priorHash = existed.getFileHash();
        String currentHash = importContext.getFileHash();
        if (StringUtils.isNotBlank(priorHash)) {
            if (StringUtils.isBlank(currentHash)) {
                return null;
            }
            if (!priorHash.equals(currentHash)) {
                result.setSuccess(false);
                result.setComplete(false);
                result.setIdempotent(false);
                result.setErrorMsg("相同业务键已存在不同文件的导入结果，拒绝执行");
                result.setTotalCount(0L);
                result.setSuccessCount(0);
                result.setFailCount(0);
                return result;
            }
        }
        String status = existed.getStatus();
        boolean successLike = Boolean.TRUE.equals(existed.isSuccess())
            || ImportStatusEnum.SUCCESS.getCode().equals(status)
            || ImportStatusEnum.PARTIAL_SUCCESS.getCode().equals(status)
            || "SUCCESS".equalsIgnoreCase(status)
            || "PARTIAL_SUCCESS".equalsIgnoreCase(status);
        result.setSuccess(successLike);
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
                .fileHash(info.getFileHash())
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
