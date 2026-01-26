package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.request.ImportCallbackRequest;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.executor.reader.ImportStreamReader;
import com.openquartz.easyfile.core.executor.writer.ImportResultWriter;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.storage.file.UploadService;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 异步导入处理器
 *
 * @author svnee
 */
@Slf4j
@Component
public class AsyncImportHandler {

    private final ImportStorageService importStorageService;
    private final UploadService uploadService;
    private final List<ImportStreamReader> importStreamReaders;
    private final List<ImportResultWriter> importResultWriters;
    private final IEasyFileDownloadProperty downloadProperty;

    public AsyncImportHandler(ImportStorageService importStorageService,
        UploadService uploadService,
        List<ImportStreamReader> importStreamReaders,
        List<ImportResultWriter> importResultWriters,
        IEasyFileDownloadProperty downloadProperty) {
        this.importStorageService = importStorageService;
        this.uploadService = uploadService;
        this.importStreamReaders = importStreamReaders;
        this.importResultWriters = importResultWriters;
        this.downloadProperty = downloadProperty;
    }

    @SuppressWarnings("unchecked")
    public void execute(BaseAsyncImportExecutor executor, String fileUrl, Long registerId) {
        if (!importStorageService.enableRunning(registerId)) {
            log.warn("Import task is not enabled to run. registerId: {}", registerId);
            return;
        }

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<Pair<Object, String>> allFailures = new ArrayList<>();

        try {
            // 1. Download/Open Stream
            log.info("Starting import for registerId: {}, fileUrl: {}", registerId, fileUrl);

            String fileName = getFileName(fileUrl);
            ImportStreamReader streamReader = importStreamReaders.stream()
                .filter(r -> r.support(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No suitable reader for file: " + fileName));

            BaseImportRequestContext context = new BaseImportRequestContext();

            try (InputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream())) {
                streamReader.read(inputStream, executor.getDataClass(), executor.batchSize(), batch -> {
                    List<Pair<Object, String>> failures = executor.importData(batch, context);
                    if (failures != null && !failures.isEmpty()) {
                        allFailures.addAll(failures);
                        fail.addAndGet(failures.size());
                        success.addAndGet(batch.size() - failures.size());
                    } else {
                        success.addAndGet(batch.size());
                    }
                });
            }

            // 2. Generate Error File
            String errorFileUrl = StringUtils.EMPTY;
            if (!allFailures.isEmpty()) {
                ImportResultWriter resultWriter = importResultWriters.stream()
                    .filter(w -> w.support(fileName))
                    .findFirst()
                    .orElse(null);

                if (resultWriter != null) {
                    File errorFile = resultWriter.writeErrorFile(allFailures, fileName);
                    if (errorFile != null && errorFile.exists()) {
                        try {
                            Pair<String, String> uploadResult = uploadService
                                .upload(errorFile, errorFile.getName(), downloadProperty.getAppId());
                            errorFileUrl = uploadResult.getValue();
                        } finally {
                            deleteFile(errorFile);
                        }
                    }
                } else {
                    log.warn("No suitable writer for error file: {}", fileName);
                }
            }

            // 3. Update Result
            ImportCallbackRequest callback = new ImportCallbackRequest();
            callback.setRegisterId(registerId);
            callback.setUploadStatus(UploadStatusEnum.SUCCESS);
            callback.setSuccessRows(success.get());
            callback.setFailRows(fail.get());
            callback.setTotalRows(success.get() + fail.get());
            callback.setErrorFileUrl(errorFileUrl);

            importStorageService.updateImportResult(callback);

        } catch (Exception e) {
            log.error("Import execution failed for registerId: {}", registerId, e);
            ImportCallbackRequest callback = new ImportCallbackRequest();
            callback.setRegisterId(registerId);
            callback.setUploadStatus(UploadStatusEnum.FAIL);
            callback.setErrorMsg(e.getMessage());
            importStorageService.updateImportResult(callback);
        }
    }

    private String getFileName(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        try {
            return new File(new URL(url).getPath()).getName();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            try {
                if (!file.delete()) {
                    log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warn("Failed to delete temp file: {}", file.getAbsolutePath(), e);
            }
        }
    }
}
