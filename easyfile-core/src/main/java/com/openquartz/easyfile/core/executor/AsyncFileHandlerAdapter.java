package com.openquartz.easyfile.core.executor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.FileSuffixEnum;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.CompressUtils;
import com.openquartz.easyfile.common.util.DateFormatUtils;
import com.openquartz.easyfile.common.util.ExceptionUtils;
import com.openquartz.easyfile.common.util.FileUtils;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.PageTotalContext;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.exception.GenerateFileErrorCode;
import com.openquartz.easyfile.core.exception.GenerateFileException;
import com.openquartz.easyfile.core.executor.bean.GenerateFileResult;
import com.openquartz.easyfile.core.executor.bean.HandleFileResult;
import com.openquartz.easyfile.core.executor.process.ExecuteProcessReporterImpl;
import com.openquartz.easyfile.core.intercept.DownloadExecutorInterceptor;
import com.openquartz.easyfile.core.intercept.ExecutorInterceptorSupport;
import com.openquartz.easyfile.core.intercept.InterceptorContext;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.file.UploadService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步处理器适配器
 * 提供异步下载的默认实现
 *
 * @author svnee
 */
public abstract class AsyncFileHandlerAdapter implements BaseAsyncFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFileHandlerAdapter.class);
    private final IEasyFileDownloadProperty downloadProperties;
    private final UploadService uploadService;
    private final DownloadStorageService downloadStorageService;

    public AsyncFileHandlerAdapter(IEasyFileDownloadProperty downloadProperties, UploadService uploadService,
        DownloadStorageService storageService) {
        this.downloadProperties = downloadProperties;
        this.downloadStorageService = storageService;
        this.uploadService = uploadService;
    }

    @Override
    public boolean handle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        ExportResult exportResult = this.handleResult(executor, baseRequest, registerId);
        return UploadStatusEnum.SUCCESS.equals(exportResult.getUploadStatus());
    }

    @Override
    public ExportResult handleResult(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest,
        Long registerId) {
        // 查询导出执行器FileExportExecutor
        Class<?> clazz = SpringContextUtil.getRealClass(executor);
        FileExportExecutor exportExecutor = clazz.getDeclaredAnnotation(FileExportExecutor.class);
        // 如果开启缓存-调用远程服务查询缓存
        if (executor.enableExportCache(baseRequest)) {
            LoadingExportCacheRequest cacheRequest =
                buildLoadingExportCacheRequest(baseRequest, exportExecutor, registerId);
            ExportResult exportResult = downloadStorageService.loadingCacheExportResult(cacheRequest);
            if (Objects.nonNull(exportResult) && UploadStatusEnum.SUCCESS.equals(exportResult.getUploadStatus())) {
                return exportResult;
            }
        }

        // 校验是否可以执行运行
        if (!downloadStorageService.enableRunning(registerId)) {
            logger.error("[AsyncFileHandlerAdapter#handleResult] this registerId has running!skip,registerId:{}",
                registerId);
            return buildDefaultRejectExportResult(registerId);
        }

        Pair<String, String> fileUrl;
        boolean handleBreakFlag;
        GenerateFileResult genFileResult = null;
        String localFileName;
        try {
            HandleFileResult handleFileResult = handleFileWithRetry(executor, exportExecutor, baseRequest, registerId);
            genFileResult = handleFileResult.getGenFileResult();
            handleBreakFlag = handleFileResult.getGenFileResult().isHandleBreakFlag();
            fileUrl = handleFileResult.getFileUrlPair();
            localFileName = handleFileResult.getLocalFileName();
        } finally {
            // 上传完成时执行文件删除操作
            if (Objects.nonNull(genFileResult)) {
                genFileResult.destroy(logger, downloadProperties.isCleanFileAfterUpload());
            }
        }

        UploadCallbackRequest request = new UploadCallbackRequest();
        request.setRegisterId(registerId);
        request.setSystem(Objects.nonNull(fileUrl) ? fileUrl.getKey() : Constants.NONE_FILE_SYSTEM);
        request.setFileName(localFileName);
        if (!handleBreakFlag) {
            request.setUploadStatus(UploadStatusEnum.SUCCESS);
            request.setFileUrl(Objects.nonNull(fileUrl) ? fileUrl.getValue() : StringUtils.EMPTY);
        } else {
            request.setUploadStatus(UploadStatusEnum.FAIL);
            Optional.of(genFileResult).ifPresent(k -> request.setErrorMsg(lessErrorMsg(k.getErrorMsg())));
        }
        downloadStorageService.uploadCallback(request);
        // 上传完成结果进行回调触发。
        ExportResult exportResult = buildExportResult(request);
        executor.asyncCompleteCallback(exportResult, baseRequest);
        return exportResult;
    }

    private void afterHandle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest,
        ExportResult result, InterceptorContext interceptorContext) {
        ExecutorInterceptorSupport.getInterceptors().stream()
            .sorted(((o1, o2) -> o2.order() - o1.order()))
            .forEach(interceptor -> interceptor.afterExecute(executor, baseRequest, result, interceptorContext));
    }

    private void beforeHandle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest,
        Long registerId, InterceptorContext interceptorContext) {
        ExecutorInterceptorSupport.getInterceptors().stream()
            .sorted((Comparator.comparingInt(DownloadExecutorInterceptor::order)))
            .forEach(interceptor -> interceptor.beforeExecute(executor, baseRequest, registerId, interceptorContext));
    }

    /**
     * 做执行执行器
     *
     * @param executor 执行器
     * @param baseRequest 请求
     * @param registerId 注册ID
     */
    public void doExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        logger.info("[AsyncFileHandlerAdapter#execute]start,execute!registerId:{}", registerId);
        ExportResult result = null;
        InterceptorContext interceptorContext = InterceptorContext.newInstance();
        try {
            // 执行拦截前置处理
            beforeHandle(executor, baseRequest, registerId, interceptorContext);
            // 执行拦截
            result = handleResult(executor, baseRequest, registerId);
        } catch (Exception ex) {
            logger.error("[AsyncFileHandlerAdapter#execute]end,execute error!registerId:{}", registerId, ex);
            ExceptionUtils.rethrow(ex);
        } finally {
            // 执行拦截后置处理
            afterHandle(executor, baseRequest, result, interceptorContext);
            PageTotalContext.clear();
        }
        logger.info("[AsyncFileHandlerAdapter#execute]end,execute!registerId:{}", registerId);
    }

    private LoadingExportCacheRequest buildLoadingExportCacheRequest(BaseDownloaderRequestContext baseRequest,
        FileExportExecutor exportExecutor,
        Long registerId) {
        LoadingExportCacheRequest loadingExportCacheRequest = new LoadingExportCacheRequest();
        loadingExportCacheRequest.setRegisterId(registerId);
        loadingExportCacheRequest.setCacheKeyList(CollectionUtils.newArrayList(exportExecutor.cacheKey()));
        loadingExportCacheRequest.setExportParamMap(baseRequest.getOtherMap());
        return loadingExportCacheRequest;
    }

    /**
     * 生成文件方法
     *
     * @param executor executor 执行器
     * @param baseRequest baseRequest 基础下载请求
     * @param registerId 注册ID
     * @param exportExecutor 执行器注解
     * @param tempLocalFilePath 本地文件临时存放目录
     * @return 生成文件结果
     */
    private GenerateFileResult generateFile(BaseDownloadExecutor executor,
        FileExportExecutor exportExecutor,
        BaseDownloaderRequestContext baseRequest,
        Long registerId,
        String tempLocalFilePath) {

        // 生成英文版文件名
        long startTime = System.currentTimeMillis();
        String fileName = generateEnFileName(baseRequest.getFileSuffix(), exportExecutor, tempLocalFilePath);
        File file = new File(fileName);
        File parentFile = file.getParentFile();

        // 异常信息
        StringJoiner errorMsgJoiner = new StringJoiner("|");
        // 处理中断标记
        boolean handleBreakFlag = false;
        boolean compress;

        // 能创建多级目录
        if (!parentFile.exists()) {
            try {
                boolean mkdirs = parentFile.mkdirs();
                Asserts.isTrue(mkdirs, GenerateFileErrorCode.CREATE_LOCAL_TEMP_FILE_ERROR);
            } catch (Exception ex) {
                logger.error(
                    "[AbstractAsyncFileHandlerAdapter#generateFile] create dictionary error,registerId:{},downloadCode:{}",
                    registerId, exportExecutor.value(), ex);
                handleBreakFlag = true;
                errorMsgJoiner.add(ex.getMessage());
            }
        }
        if (!file.exists()) {
            try {
                boolean fileHasName = file.createNewFile();
                Asserts.isTrue(fileHasName, GenerateFileErrorCode.FILE_NAME_DUPLICATE_ERROR,
                    GenerateFileException.class);
            } catch (Exception ex) {
                logger.error(
                    "[AbstractAsyncFileHandlerAdapter#generateFile] handle,create new file error,registerId:{},downloadCode:{},path:{}",
                    registerId, exportExecutor.value(), downloadProperties.getLocalFileTempPath(), ex);
                errorMsgJoiner.add("创建新文件异常,文件路径:" + downloadProperties.getLocalFileTempPath());
                handleBreakFlag = true;
            }
        }
        if (!handleBreakFlag) {
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {

                //report execute-process
                ExecuteProcessReporterImpl reporter = new ExecuteProcessReporterImpl(registerId,
                    downloadStorageService);
                ExecuteProcessProbe.setCurrentReporter(reporter);
                reporter.start();

                DownloaderRequestContext requestContext = buildRequestDownloaderRequest(out, baseRequest);
                executor.export(requestContext);

                // report complete
                reporter.complete();
            } catch (Exception exception) {
                logger.error("[AbstractAsyncFileHandlerAdapter#handle] execute file error,downloadCode:{}",
                    exportExecutor.value(),
                    exception);
                errorMsgJoiner.add("导出文件逻辑异常:" + exception.getMessage());
                handleBreakFlag = true;
            } finally {
                ExecuteProcessProbe.clear();
            }
        }
        // 执行文件压缩
        Pair<Boolean, File> compressResult = compress(file, handleBreakFlag);
        compress = compressResult.getKey();
        logger.info(
            "[AbstractAsyncFileHandlerAdapter#generateFile] registerId:{},downloadCode:{},generate-file bytes:{} kb,cost-time:{}",
            registerId, exportExecutor.value(), FileUtils.sizeOfKb(file), System.currentTimeMillis() - startTime);
        return GenerateFileResult
            .build(errorMsgJoiner, file, compressResult.getValue(), handleBreakFlag, compress);
    }

    /**
     * 压缩文件
     *
     * @param file 文件
     * @param handleBreakFlag 处理中断标记
     * @return key:是否执行压缩成功/value:压缩文件
     */
    private Pair<Boolean, File> compress(final File file, boolean handleBreakFlag) {
        if (handleBreakFlag || !downloadProperties.isEnableCompressFile()) {
            return Pair.of(false, null);
        }
        // 是正常未压缩的文件.且文件当前的大小大于文件压缩的阀值
        boolean isExeCompress = file.exists()
            && file.isFile()
            && !isZipCompress(file)
            && (downloadProperties.getMinEnableCompressMbSize() <= 0
            || FileUtils.sizeOfMb(file) >= downloadProperties.getMinEnableCompressMbSize());
        if (isExeCompress) {
            // 直接执行压缩
            // 获取文件的绝对路径
            try {
                String path = file.getAbsolutePath();
                CompressUtils.zip(path, path + FileSuffixEnum.ZIP.getFullFileSuffix());
                return Pair.of(Boolean.TRUE, new File(path + FileSuffixEnum.ZIP.getFullFileSuffix()));
            } catch (Exception ex) {
                logger
                    .error("[AsyncFileHandleAdapter#compress] compress file fail! path:{}", file.getAbsolutePath(), ex);
            }
        }
        return Pair.of(Boolean.FALSE, null);
    }

    /**
     * 是否已经是压缩文件
     *
     * @param file 文件
     * @return 是否已经是zip压缩文件
     */
    private boolean isZipCompress(File file) {
        if (file.getName().contains(Constants.FILE_SUFFIX_SEPARATOR)) {
            return false;
        }
        String[] fileNameSplit = file.getName().split("\\.");
        return fileNameSplit.length > 1 && FileSuffixEnum.ZIP.getCode().equals(fileNameSplit[fileNameSplit.length - 1]);
    }

    /**
     * 处理文件&重试执行
     *
     * @param executor 执行器
     * @param exportExecutor 执行器注解
     * @param baseRequest 请求
     * @param registerId 注册ID
     * @return 处理文件结果
     */
    private HandleFileResult handleFileWithRetry(BaseDownloadExecutor executor,
        FileExportExecutor exportExecutor,
        BaseDownloaderRequestContext baseRequest,
        Long registerId) {

        // 针对IO 异常重试3次
        Retryer<HandleFileResult> retryer = RetryerBuilder.<HandleFileResult>newBuilder()
            .retryIfExceptionOfType(GenerateFileException.class)
            .withWaitStrategy(WaitStrategies.incrementingWait(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .build();
        // 生成文件
        GenerateFileResult genFileResult = generateFile(executor, exportExecutor, baseRequest, registerId,
            downloadProperties.getLocalFileTempPath());
        try {
            return retryer.call(() -> {
                Pair<String, String> fileUrl = null;
                Throwable error = null;
                String cnFileName = StringUtils.EMPTY;
                if (!genFileResult.isHandleBreakFlag()) {
                    try {
                        cnFileName = genCnFileName(baseRequest.getFileSuffix(), exportExecutor,
                            genFileResult.isCompress());
                        fileUrl = uploadService
                            .upload(genFileResult.getUploadFile(), cnFileName, downloadProperties.getAppId());
                    } catch (GenerateFileException ex) {
                        throw ex;
                    } catch (Exception t) {
                        genFileResult.setHandleBreakFlag(true);
                        genFileResult.getErrorMsg().add("[文件上传]" + t.getMessage());
                        error = t;
                    }
                }
                return new HandleFileResult(genFileResult, fileUrl, error).localFileName(cnFileName);
            });
        } catch (Exception e) {
            logger.error(
                "[AsyncFileHandlerAdapter#handleFileWithRetry] retry execute handle file,error!registerId:{},executor:{}",
                registerId, exportExecutor.value(), e);
            genFileResult.setHandleBreakFlag(true);
            genFileResult.getErrorMsg().add("[文件处理]" + e.getMessage());
            return new HandleFileResult(genFileResult, null, e);
        }
    }


    /**
     * 获取最小errorMsg
     *
     * @param errorMsgJoiner joiner
     * @return errorMsg
     */
    private String lessErrorMsg(StringJoiner errorMsgJoiner) {
        if (Objects.isNull(errorMsgJoiner)) {
            return StringUtils.EMPTY;
        }
        String errorMsg = errorMsgJoiner.toString();
        if (StringUtils.isNotBlank(errorMsg) && errorMsg.length() > Constants.MAX_UPLOAD_ERROR_MSG_LENGTH) {
            errorMsg = errorMsg.substring(0, Constants.MAX_UPLOAD_ERROR_MSG_LENGTH);
        }
        return errorMsg;
    }

    private ExportResult buildExportResult(UploadCallbackRequest request) {
        ExportResult exportResult = new ExportResult();
        exportResult.setRegisterId(request.getRegisterId());
        exportResult.setUploadStatus(request.getUploadStatus());
        exportResult.setFileSystem(request.getSystem());
        exportResult.setFileName(request.getFileName());
        exportResult.setFileUrl(request.getFileUrl());
        exportResult.setErrorMsg(request.getErrorMsg());
        return exportResult;
    }

    private ExportResult buildDefaultRejectExportResult(Long registerId) {
        ExportResult exportResult = new ExportResult();
        exportResult.setRegisterId(registerId);
        exportResult.setUploadStatus(UploadStatusEnum.FAIL);
        exportResult.setFileSystem(Constants.NONE_FILE_SYSTEM);
        exportResult.setFileUrl(StringUtils.EMPTY);
        exportResult.setFileName(StringUtils.EMPTY);
        return exportResult;
    }

    private DownloaderRequestContext buildRequestDownloaderRequest(OutputStream out,
        BaseDownloaderRequestContext baseRequest) {
        DownloaderRequestContext downloaderRequest = new DownloaderRequestContext();
        downloaderRequest.setOut(out);
        downloaderRequest.setNotifier(baseRequest.getNotifier());
        downloaderRequest.setFileSuffix(baseRequest.getFileSuffix());
        downloaderRequest.setExportRemark(baseRequest.getExportRemark());
        downloaderRequest.setOtherMap(baseRequest.getOtherMap());
        return downloaderRequest;
    }

    private String genCnFileName(String fileSuffix, FileExportExecutor executor, boolean compress) {
        String fileName = StringUtils.isNotBlank(executor.desc()) ? executor.desc() : executor.value();
        String executeTime = DateFormatUtils.format(new Date(), "yyMMddHHmmssSSS");
        String suffix = fileSuffix.startsWith(Constants.FILE_SUFFIX_SEPARATOR) ? fileSuffix
            : Constants.FILE_SUFFIX_SEPARATOR + fileSuffix;
        // 如果是开启了压缩的文件,使用压缩文件的后缀名.zip 否则不变更
        return fileName + "_" + executeTime + suffix + (compress ? FileSuffixEnum.ZIP.getFullFileSuffix()
            : StringUtils.EMPTY);
    }

    private String generateEnFileName(String fileSuffix, FileExportExecutor executor, String path) {
        String newPath = path.endsWith(File.separator) ? path : path + File.separator;
        String fileName = executor.value();
        String executeTime = DateFormatUtils.format(new Date(), "yyMMddHHmmssSSS");
        String suffix = fileSuffix.startsWith(Constants.FILE_SUFFIX_SEPARATOR) ? fileSuffix
            : Constants.FILE_SUFFIX_SEPARATOR + fileSuffix;
        return newPath + fileName + "_" + executeTime + suffix;
    }
}
