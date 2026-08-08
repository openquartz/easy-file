package com.openquartz.easyfile.core.executor;

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
import com.openquartz.easyfile.core.service.IdempotentImportManager;
import com.openquartz.easyfile.core.ImportResultFileGenerator;
import com.openquartz.easyfile.core.exception.ConcurrentImportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.file.DownloadService;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

        try {
            reporter.start();
            ImportExecuteProcessProbe.setCurrentReporter(reporter);

            // 1. 下载文件
            File tempFile = downloadFile(context);
            result.setTempFilePath(tempFile.getAbsolutePath());

            // 2. 创建解析器
            parser = createParser(tempFile, context.getFileType());

            // 3. 获取总记录数
            long totalCount = parser.getTotalCount();
            importStorageService.updateTotalCount(context.getRegisterId(), totalCount);

            // 4. 分页处理
            ImportPagedResult pagedResult = processByPage(context, parser, totalCount);

            // 5. 生成结果文件
            generateResultFile(context, pagedResult);

            // 6. 设置结果（无失败才算成功）
            boolean success = pagedResult.getFailCount() == 0;
            result.setSuccess(success);
            result.setTotalCount(totalCount);
            result.setSuccessCount(pagedResult.getSuccessCount());
            result.setFailCount(pagedResult.getFailCount());
            result.setErrorRecords(pagedResult.getErrorRecords());
            result.setComplete(true);

            // 7. 仅真正成功时做幂等标记
            if (success) {
                ImportResult importResult = buildImportResult(result);
                idempotentManager.markAsExecuted(context.getBusinessUniqueKey(), importResult);
            }

        } catch (Exception e) {
            log.error("导入处理失败: registerId={}", context.getRegisterId(), e);
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
                // 更新最终状态
                long resumePosition = result.isSuccess() ? result.getTotalCount()
                    : (long) result.getSuccessCount() + result.getFailCount();
                importStorageService.complete(
                    context.getRegisterId(),
                    result.isSuccess() ? ImportStatusEnum.SUCCESS : ImportStatusEnum.FAIL,
                    result.getTotalCount(),
                    (long) result.getSuccessCount(),
                    (long) result.getFailCount(),
                    resumePosition,
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
    private ImportPagedResult processByPage(ImportContext context,
                                           ImportParser<T> parser,
                                           long totalCount) {
        ImportPagedResult pagedResult = ImportPagedResult.builder().build();
        pagedResult.setTotalCount(totalCount);

        int pageSize = getPageSize();
        int totalPages = (int) ((totalCount + pageSize - 1) / pageSize);

        long resumePosition = context.getResumePosition() == null ? 0L : context.getResumePosition();
        int startPage = (int) (resumePosition / pageSize) + 1;

        log.info("开始分页处理: registerId={}, 总记录数={}, 分页大小={}, 起始页={}",
            context.getRegisterId(), totalCount, pageSize, startPage);

        int successCount = 0;
        int failCount = 0;
        List<ErrorRecord> errorRecords = new ArrayList<>();

        for (int pageNum = startPage; pageNum <= totalPages; pageNum++) {
            try {
                long startRow = (pageNum - 1L) * pageSize + 1L;
                List<ImportRow<T>> importRows = parser.readPage(startRow, pageSize);
                List<T> pageData = new ArrayList<>(importRows.size());
                for (ImportRow<T> importRow : importRows) {
                    if (importRow.getParseError() != null || importRow.getData() == null) {
                        failCount++;
                        errorRecords.add(ErrorRecord.of(
                            (int) importRow.getRowNumber(),
                            importRow.getParseError() != null ? importRow.getParseError() : "行解析失败"));
                        continue;
                    }
                    pageData.add(importRow.getData());
                }

                if (pageData.isEmpty()) {
                    continue;
                }

                // 验证数据
                ValidationResult validationResult = validate(
                    ImportDataContext.<T>builder()
                        .data(pageData)
                        .requestContext(context)
                        .pageNumber(pageNum)
                        .pageSize(pageSize)
                        .totalCount(totalCount)
                        .build());

                if (!validationResult.isSuccess()) {
                    List<ErrorRecord> errors = validationResult.getErrors() == null
                        ? Collections.emptyList() : validationResult.getErrors();
                    errorRecords.addAll(errors);
                    failCount += validationResult.getErrorCount() > 0
                        ? validationResult.getErrorCount() : pageData.size();
                    continue;
                }

                // 业务处理
                ProcessResult processResult = processBusiness(pageData,
                    ImportProcessContext.builder()
                        .requestContext(context)
                        .pageNumber(pageNum)
                        .pageStartPosition((long) (pageNum - 1) * pageSize)
                        .build());

                if (processResult.isSuccess()) {
                    successCount += pageData.size();
                } else {
                    failCount += pageData.size();
                    List<ErrorRecord> errors = processResult.getErrors() == null
                        ? Collections.emptyList() : processResult.getErrors();
                    errorRecords.addAll(errors);
                }

                // 更新进度
                int process = totalCount <= 0 ? 100 : (int) ((pageNum * pageSize * 100L) / totalCount);
                long processedCount = (long) pageNum * pageSize;
                if (processedCount > totalCount) {
                    processedCount = totalCount;
                }
                ImportExecuteProcessProbe.report(process, processedCount);

            } catch (Exception e) {
                log.error("处理第{}页失败: registerId={}", pageNum, context.getRegisterId(), e);
                int remaining = (int) Math.max(0, totalCount - (long) (pageNum - 1) * pageSize);
                failCount += Math.min(pageSize, remaining);
                errorRecords.add(ErrorRecord.of(pageNum, e.getMessage()));
            }
        }

        pagedResult.setSuccessCount(successCount);
        pagedResult.setFailCount(failCount);
        pagedResult.setErrorRecords(errorRecords);

        return pagedResult;
    }

    /**
     * 生成结果文件
     */
    private void generateResultFile(ImportContext context, ImportPagedResult pagedResult) {
        if (pagedResult.getErrorRecords() == null || pagedResult.getErrorRecords().isEmpty()) {
            return; // 没有错误记录，不生成结果文件
        }

        try {
            String resultFileUrl = importResultFileGenerator.generate(context.getAppId(),
                context.getRegisterId(),
                pagedResult.getErrorRecords(),
                pagedResult.getTotalCount(),
                pagedResult.getSuccessCount(),
                pagedResult.getFailCount());

            importStorageService.updateResultFileUrl(context.getRegisterId(), resultFileUrl);

        } catch (Exception e) {
            log.error("生成结果文件失败: registerId={}", context.getRegisterId(), e);
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
    private ImportResult buildImportResult(ImportProcessResult processResult) {
        return ImportResult.builder()
            .registerId(processResult.getRegisterId())
            .success(processResult.isSuccess())
            .process(processResult.isSuccess() ? 100 : 0)
            .totalCount(processResult.getTotalCount())
            .successCount((long) processResult.getSuccessCount())
            .failCount((long) processResult.getFailCount())
            .resultFileUrl(null) // 需要单独设置
            .errorMsg(processResult.getErrorMsg())
            .status(processResult.isSuccess() ? "COMPLETED" : "FAILED")
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
}
