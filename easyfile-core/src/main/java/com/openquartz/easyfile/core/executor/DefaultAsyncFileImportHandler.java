package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.bean.ImportRequestContext;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * import data file handler
 *
 * @author svnee
 */
public class DefaultAsyncFileImportHandler implements BaseAsyncFileImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncFileImportHandler.class);

    private final DownloadStorageService downloadStorageService;

    public DefaultAsyncFileImportHandler(DownloadStorageService downloadStorageService) {
        this.downloadStorageService = downloadStorageService;
    }

    @Override
    public ImportResult handleResult(BaseImportExecutor executor, BaseImportRequestContext baseRequest, Long registerId) {

        Class<?> clazz = SpringContextUtil.getRealClass(executor);
        FileImportExecutor importExecutor = clazz.getDeclaredAnnotation(FileImportExecutor.class);

        if (!downloadStorageService.enableRunning(registerId)) {
            logger.error("[AsyncFileImportHandlerAdapter#handleResult] this registerId has running!skip,registerId:{},code:{}",
                    registerId, importExecutor.value());
            return buildDefaultRejectExportResult(registerId);
        }

        boolean importDataResult = true;
        String errorMessage = null;
        ImportRequestContext context = buildImportRequestContext(executor, baseRequest);
        try {
            executor.importData(context);
        } catch (Exception e) {
            importDataResult = false;
            errorMessage = e.getMessage();
            logger.error("[AsyncFileImportHandlerAdapter#handleResult] this registerId:{} import data failed", registerId, e);
        } finally {
            if (Objects.nonNull(context.getIn())) {
                try {
                    context.getIn().close();
                } catch (Exception ignored) {
                }
            }
        }

        UploadCallbackRequest request = new UploadCallbackRequest();
        request.setRegisterId(registerId);
        request.setSystem(Constants.NONE_FILE_SYSTEM);
        if (importDataResult) {
            request.setUploadStatus(HandleStatusEnum.SUCCESS);
        } else {
            request.setUploadStatus(HandleStatusEnum.FAIL);
            String finalErrorMessage = errorMessage;
            Optional.ofNullable(errorMessage).ifPresent(k -> request.setErrorMsg(finalErrorMessage));
        }
        downloadStorageService.uploadCallback(request);
        // 上传完成结果进行回调触发。
        ImportResult importResult = buildImportResult(request);
        executor.asyncCompleteCallback(importResult, baseRequest);
        return importResult;
    }

    private ImportResult buildImportResult(UploadCallbackRequest request) {
        ImportResult importResult = new ImportResult();
        importResult.setRegisterId(request.getRegisterId());
        importResult.setHandleStatus(request.getUploadStatus());
        importResult.setErrorMsg(request.getErrorMsg());
        return importResult;
    }

    private ImportRequestContext buildImportRequestContext(BaseImportExecutor executor, BaseImportRequestContext baseRequest) {

        BaseImportRequestContext baseImportRequestContext = new BaseImportRequestContext();
        baseImportRequestContext.setNotifier(baseRequest.getNotifier());
        baseImportRequestContext.putParam(baseRequest.getOtherMap());
        baseImportRequestContext.setImportRemark(baseRequest.getImportRemark());

        // input io stream
        InputStream inputStream = executor.importSource(baseImportRequestContext);

        ImportRequestContext importRequestContext = new ImportRequestContext();
        importRequestContext.setIn(inputStream);
        importRequestContext.setNotifier(baseRequest.getNotifier());
        importRequestContext.putParam(baseRequest.getOtherMap());
        importRequestContext.setImportRemark(baseRequest.getImportRemark());
        return importRequestContext;
    }

    private ImportResult buildDefaultRejectExportResult(Long registerId) {
        ImportResult importResult = new ImportResult();
        importResult.setRegisterId(registerId);
        importResult.setErrorMsg(StringUtils.EMPTY);
        importResult.setHandleStatus(HandleStatusEnum.FAIL);
        return importResult;
    }
}
