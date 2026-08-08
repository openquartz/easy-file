package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.util.FileUtils;
import com.openquartz.easyfile.common.util.ImportTempFiles;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.lock.LockBizType;
import com.openquartz.easyfile.common.lock.LockSupport;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.core.context.*;
import com.openquartz.easyfile.core.entity.*;
import com.openquartz.easyfile.core.parser.ImportParser;
import com.openquartz.easyfile.core.parser.ImportParserFactory;
import com.openquartz.easyfile.core.parser.ImportRow;
import com.openquartz.easyfile.core.executor.process.ImportExecuteProcessReporterImpl;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.core.ImportResultFileGenerator;
import com.openquartz.easyfile.core.exception.ConcurrentImportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.file.DownloadService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 分页导入执行器抽象基类
 * 实现防OOM的分页导入逻辑
 *
 * @author svnee
 */
@Slf4j
public abstract class AbstractPagedImportExecutor<T> implements BaseImportExecutor<T> {

    @Autowired
    protected IdempotentImportManager idempotentManager;

    @Autowired
    protected LockSupport lockSupport;

    @Autowired
    protected ImportStorageService importStorageService;

    @Autowired
    protected DownloadService downloadService;

    @Autowired
    protected ImportResultFileGenerator importResultFileGenerator;

    private static final String TEMP_DIR = "/tmp/easyfile/import/";

    @Override
    public ImportProcessResult process(ImportContext context) {
        return executeWithConcurrencyControl(context, () -> doProcess(context));
    }

    /**
     * 执行导入处理（带并发控制）
     */
    private ImportProcessResult executeWithConcurrencyControl(ImportContext context,
                                                              java.util.function.Supplier<ImportProcessResult> task) {
        try {
            final ImportProcessResult[] resultHolder = new ImportProcessResult[1];
            boolean locked = lockSupport.consumeIfTryLock(
                Pair.of(context.getBusinessUniqueKey(), LockBizType.IMPORT_RUNNING_LOCK), () -> {
                // 幂等性检查
                if (idempotentManager.isExecuted(context.getBusinessUniqueKey())) {
                    resultHolder[0] = buildIdempotentResult(context);
                    return;
                }
                resultHolder[0] = task.get();
            });
            if (!locked) {
                throw new ConcurrentImportException("系统繁忙，请稍后重试");
            }
            return resultHolder[0];
        } catch (Exception e) {
            log.error("导入处理失败: registerId={}", context.getRegisterId(), e);
            throw e;
        }
    }

    /**
     * 实际执行导入处理
     */
    private ImportProcessResult doProcess(ImportContext context) {
        ImportProcessResult result = ImportProcessResult.builder().build();
        result.setRegisterId(context.getRegisterId());
        result.setStartTime(new Date());
        ImportExecuteProcessReporterImpl reporter =
            new ImportExecuteProcessReporterImpl(context.getRegisterId(), importStorageService);
        ImportParser<T> parser = null;
        ImportStatusEnum terminalStatus = ImportStatusEnum.FAIL;
        long lastResumePosition = context.getResumePosition() == null ? 0L : context.getResumePosition();

        try {
            reporter.start();
            ImportExecuteProcessProbe.setCurrentReporter(reporter);

            // 1. 下载文件
            File tempFile = downloadFile(context);
            result.setTempFilePath(tempFile.getAbsolutePath());

            // 2. 创建解析器（工厂按 fileType 选择）
            parser = createParser(tempFile, context.getFileType());

            // 3. 获取总记录数
            long totalCount = parser.getTotalCount();
            importStorageService.updateTotalCount(context.getRegisterId(), totalCount);

            // 4. 分页处理
            PageLoopOutcome pagedResult = processByPage(context, parser, totalCount);
            lastResumePosition = pagedResult.resumePosition;
            result.setTotalCount(totalCount);
            result.setSuccessCount(pagedResult.successCount);
            result.setFailCount(pagedResult.failCount);
            result.setErrorRecords(pagedResult.errorRecords);

            if (pagedResult.cancelled) {
                terminalStatus = ImportStatusEnum.CANCEL;
                result.setSuccess(false);
                result.setComplete(true);
                result.setErrorMsg("任务已取消");
            } else if (pagedResult.taskFailed) {
                terminalStatus = ImportStatusEnum.FAIL;
                result.setSuccess(false);
                result.setComplete(false);
                result.setErrorMsg(pagedResult.errorMsg);
            } else {
                // 5. 生成结果文件（行级错误时必填；失败则降级 FAIL）
                boolean resultFileOk = generateResultFile(context, pagedResult);
                if (pagedResult.failCount > 0) {
                    if (!resultFileOk) {
                        terminalStatus = ImportStatusEnum.FAIL;
                        result.setSuccess(false);
                        result.setComplete(false);
                        result.setErrorMsg("结果文件生成失败");
                    } else {
                        terminalStatus = ImportStatusEnum.PARTIAL_SUCCESS;
                        result.setSuccess(false);
                        result.setComplete(true);
                        lastResumePosition = totalCount;
                    }
                } else {
                    terminalStatus = ImportStatusEnum.SUCCESS;
                    result.setSuccess(true);
                    result.setComplete(true);
                    lastResumePosition = totalCount;
                }
            }

            // 6. 成功态（SUCCESS / PARTIAL_SUCCESS）做幂等标记
            if (terminalStatus == ImportStatusEnum.SUCCESS
                || terminalStatus == ImportStatusEnum.PARTIAL_SUCCESS) {
                ImportResult importResult = buildImportResult(result, terminalStatus);
                idempotentManager.markAsExecuted(context.getBusinessUniqueKey(), importResult);
            }

        } catch (Exception e) {
            log.error("导入处理失败: registerId={}", context.getRegisterId(), e);
            terminalStatus = ImportStatusEnum.FAIL;
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
            result.setComplete(false);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    log.warn("关闭解析器失败: registerId={}", context.getRegisterId(), e);
                }
            }
            result.setEndTime(new Date());
            result.setDuration(result.getEndTime().getTime() - result.getStartTime().getTime());

            // 清理临时文件
            cleanupTempFile(result.getTempFilePath());

            try {
                importStorageService.complete(
                    context.getRegisterId(),
                    terminalStatus,
                    result.getTotalCount(),
                    (long) result.getSuccessCount(),
                    (long) result.getFailCount(),
                    lastResumePosition,
                    result.getErrorMsg()
                );
            } finally {
                ImportExecuteProcessProbe.clear();
            }
        }

        return result;
    }

    /**
     * 下载文件
     */
    private File downloadFile(ImportContext context) {
        String ossUrl = context.getOssUrl();
        String tempDir = getTempDirectory();

        File tempFile = ImportTempFiles.resolveTempFile(
            tempDir, context.getFileName(), context.getRegisterId(), context.getFileType());

        tempFile.getParentFile().mkdirs();

        // 下载文件
        downloadService.download(ossUrl, tempFile);

        // 计算文件哈希
        String fileHash = FileUtils.md5(tempFile);
        importStorageService.updateFileHash(context.getRegisterId(), fileHash);

        return tempFile;
    }

    /**
     * 创建解析器
     */
    protected ImportParser<T> createParser(File file, String fileType) {
        return ImportParserFactory.create(fileType, file, getTargetClass());
    }

    /**
     * 分页处理
     */
    private PageLoopOutcome processByPage(ImportContext context,
                                          ImportParser<T> parser,
                                          long totalCount) {
        PageLoopOutcome outcome = new PageLoopOutcome();
        outcome.totalCount = totalCount;

        int pageSize = getPageSize();
        int totalPages = totalCount <= 0 ? 0 : (int) ((totalCount + pageSize - 1) / pageSize);

        long resumePosition = 0L;
        if (supportResumable() && context.getResumePosition() != null) {
            resumePosition = context.getResumePosition();
        }
        outcome.resumePosition = resumePosition;
        int startPage = (int) (resumePosition / pageSize) + 1;

        log.info("开始分页处理: registerId={}, 总记录数={}, 分页大小={}, 起始页={}",
            context.getRegisterId(), totalCount, pageSize, startPage);

        int successCount = 0;
        int failCount = 0;
        List<ErrorRecord> errorRecords = new ArrayList<>();

        for (int pageNum = startPage; pageNum <= totalPages; pageNum++) {
            if (isCancelRequested(context)) {
                log.info("导入任务取消: registerId={}, pageNum={}", context.getRegisterId(), pageNum);
                outcome.cancelled = true;
                break;
            }

            long pageStartPosition = (pageNum - 1L) * pageSize;
            long pageEndResume = Math.min(pageNum * (long) pageSize, totalCount);

            try {
                long startRow = pageStartPosition + 1L;
                List<ImportRow<T>> importRows = parser.readPage(startRow, pageSize);
                List<ImportRow<T>> parsedRows = new ArrayList<>();
                List<T> pageData = new ArrayList<>();
                for (ImportRow<T> importRow : importRows) {
                    if (importRow.getParseError() != null || importRow.getData() == null) {
                        failCount++;
                        errorRecords.add(ErrorRecord.of(
                            (int) importRow.getRowNumber(),
                            importRow.getParseError() != null ? importRow.getParseError() : "行解析失败"));
                        continue;
                    }
                    parsedRows.add(importRow);
                    pageData.add(importRow.getData());
                }

                List<T> validData = pageData;
                if (!pageData.isEmpty()) {
                    ValidationResult validationResult = validate(
                        ImportDataContext.<T>builder()
                            .data(pageData)
                            .requestContext(context)
                            .pageNumber(pageNum)
                            .pageSize(pageSize)
                            .totalCount(totalCount)
                            .pageStartPosition(pageStartPosition)
                            .pageEndPosition(pageEndResume - 1)
                            .build());

                    if (!validationResult.isSuccess()) {
                        List<ErrorRecord> errors = validationResult.getErrors() == null
                            ? Collections.emptyList() : validationResult.getErrors();
                        errorRecords.addAll(errors);
                        failCount += validationResult.getErrorCount() > 0
                            ? validationResult.getErrorCount() : 0;
                        validData = resolveValidData(validationResult, parsedRows, pageData);
                        if (validationResult.getErrorCount() <= 0 && validData.isEmpty()) {
                            failCount += pageData.size();
                        }
                    }
                }

                if (!validData.isEmpty()) {
                    ProcessResult processResult;
                    try {
                        processResult = processBusiness(validData,
                            ImportProcessContext.builder()
                                .requestContext(context)
                                .pageNumber(pageNum)
                                .pageStartPosition(pageStartPosition)
                                .build());
                    } catch (Exception businessEx) {
                        log.error("处理第{}页业务失败: registerId={}", pageNum, context.getRegisterId(), businessEx);
                        outcome.taskFailed = true;
                        outcome.errorMsg = businessEx.getMessage();
                        errorRecords.add(ErrorRecord.of(pageNum, businessEx.getMessage()));
                        break;
                    }

                    if (processResult == null || !processResult.isSuccess()) {
                        outcome.taskFailed = true;
                        outcome.errorMsg = "业务处理失败";
                        List<ErrorRecord> errors = processResult == null || processResult.getErrors() == null
                            ? Collections.emptyList() : processResult.getErrors();
                        errorRecords.addAll(errors);
                        break;
                    }
                    successCount += validData.size();
                }

                // 仅页面成功（含纯行级错误、无业务失败）后推进断点
                outcome.resumePosition = pageEndResume;
                int process = totalCount <= 0 ? 100 : (int) ((pageEndResume * 100L) / totalCount);
                ImportExecuteProcessProbe.report(process, pageEndResume);

            } catch (Exception e) {
                log.error("处理第{}页失败: registerId={}", pageNum, context.getRegisterId(), e);
                outcome.taskFailed = true;
                outcome.errorMsg = e.getMessage();
                errorRecords.add(ErrorRecord.of(pageNum, e.getMessage()));
                break;
            }
        }

        outcome.successCount = successCount;
        outcome.failCount = failCount;
        outcome.errorRecords = errorRecords;
        return outcome;
    }

    @SuppressWarnings("unchecked")
    private List<T> resolveValidData(ValidationResult validationResult,
                                     List<ImportRow<T>> parsedRows,
                                     List<T> pageData) {
        if (validationResult.getValidData() != null) {
            return (List<T>) validationResult.getValidData();
        }
        List<ErrorRecord> errors = validationResult.getErrors();
        if (errors == null || errors.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> failedRows = errors.stream()
            .map(ErrorRecord::getRowNumber)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (failedRows.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> valid = new ArrayList<>();
        for (ImportRow<T> row : parsedRows) {
            if (!failedRows.contains((int) row.getRowNumber())) {
                valid.add(row.getData());
            }
        }
        return valid;
    }

    private boolean isCancelRequested(ImportContext context) {
        AtomicBoolean cancelFlag = context.getCancelFlag();
        if (cancelFlag != null && cancelFlag.get()) {
            return true;
        }
        try {
            ImportRequestInfo info = importStorageService.getRequestInfoByRegisterId(context.getRegisterId());
            return info != null && info.getImportStatus() == ImportStatusEnum.CANCEL;
        } catch (Exception ex) {
            log.debug("查询取消状态失败: registerId={}", context.getRegisterId(), ex);
            return false;
        }
    }

    /**
     * 生成结果文件
     *
     * @return true if no errors needed, or file generated successfully
     */
    private boolean generateResultFile(ImportContext context, PageLoopOutcome pagedResult) {
        if (pagedResult.errorRecords == null || pagedResult.errorRecords.isEmpty()) {
            return true;
        }

        try {
            String resultFileUrl = importResultFileGenerator.generate(context.getAppId(),
                context.getRegisterId(),
                pagedResult.errorRecords,
                pagedResult.totalCount,
                (long) pagedResult.successCount,
                (long) pagedResult.failCount);

            importStorageService.updateResultFileUrl(context.getRegisterId(), resultFileUrl);
            return resultFileUrl != null && !resultFileUrl.isEmpty();
        } catch (Exception e) {
            log.error("生成结果文件失败: registerId={}", context.getRegisterId(), e);
            return false;
        }
    }

    /**
     * 构建幂等性结果
     */
    private ImportProcessResult buildIdempotentResult(ImportContext context) {
        ImportResult existedResult = idempotentManager.getExecutedResult(context.getBusinessUniqueKey());

        ImportProcessResult result = ImportProcessResult.builder().build();
        result.setRegisterId(context.getRegisterId());
        result.setStartTime(new Date());
        result.setEndTime(new Date());
        result.setErrorRecords(new ArrayList<>());
        result.setComplete(true);
        result.setIdempotent(true);

        if (existedResult == null) {
            // executed flag exists but result missing (TTL race) — do not fabricate success
            result.setSuccess(false);
            result.setComplete(false);
            result.setErrorMsg("幂等结果不存在，请重试");
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            return result;
        }

        result.setSuccess(existedResult.isSuccess());
        result.setTotalCount(existedResult.getTotalCount());
        result.setSuccessCount(Math.toIntExact(existedResult.getSuccessCount() == null
            ? 0L : existedResult.getSuccessCount()));
        result.setFailCount(Math.toIntExact(existedResult.getFailCount() == null
            ? 0L : existedResult.getFailCount()));

        return result;
    }

    /**
     * 构建导入结果
     */
    private ImportResult buildImportResult(ImportProcessResult processResult, ImportStatusEnum status) {
        boolean successLike = status == ImportStatusEnum.SUCCESS || status == ImportStatusEnum.PARTIAL_SUCCESS;
        return ImportResult.builder()
            .registerId(processResult.getRegisterId())
            .success(successLike)
            .process(successLike ? 100 : 0)
            .totalCount(processResult.getTotalCount())
            .successCount((long) processResult.getSuccessCount())
            .failCount((long) processResult.getFailCount())
            .resultFileUrl(null)
            .errorMsg(processResult.getErrorMsg())
            .status(status.getCode())
            .startTime(processResult.getStartTime())
            .endTime(processResult.getEndTime())
            .duration(processResult.getDuration())
            .build();
    }

    /**
     * 获取临时目录
     */
    protected String getTempDirectory() {
        String tempDir = TEMP_DIR;
        new File(tempDir).mkdirs();
        return tempDir;
    }

    /**
     * 清理临时文件
     */
    protected void cleanupTempFile(String tempFilePath) {
        if (tempFilePath != null) {
            File file = new File(tempFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    // 抽象方法，由子类实现

    protected abstract Class<T> getTargetClass();

    private static final class PageLoopOutcome {
        private long totalCount;
        private int successCount;
        private int failCount;
        private List<ErrorRecord> errorRecords = new ArrayList<>();
        private long resumePosition;
        private boolean cancelled;
        private boolean taskFailed;
        private String errorMsg;
    }
}
