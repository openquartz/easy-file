package org.svnee.easyfile.starter.executor.impl;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.FileSuffixEnum;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.common.util.CompressUtils;
import org.svnee.easyfile.common.util.DateFormatUtils;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.common.exception.AssertUtil;
import org.svnee.easyfile.starter.exception.DownloadErrorCode;
import org.svnee.easyfile.starter.exception.GenerateFileErrorCode;
import org.svnee.easyfile.starter.exception.GenerateFileException;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.bean.GenerateFileResult;
import org.svnee.easyfile.starter.executor.bean.HandleFileResult;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * 异步处理器适配器
 * 提供异步下载的默认实现
 *
 * @author svnee
 */
public abstract class AsyncFileHandlerAdapter implements BaseAsyncFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFileHandlerAdapter.class);
    private final EasyFileDownloadProperties downloadProperties;
    private final UploadService uploadService;
    private final DownloadStorageService downloadStorageService;

    public AsyncFileHandlerAdapter(EasyFileDownloadProperties downloadProperties, UploadService uploadService,
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

        // 校验是否可以执行运行
        if (!downloadStorageService.enableRunning(registerId)) {
            logger.error("[AsyncFileHandlerAdapter#handleResult] this registerId has running!skip,registerId:{}",
                registerId);
            return buildDefaultRejectExportResult(registerId);
        }
        Class<?> clazz = SpringContextUtil.getRealClass(executor);
        FileExportExecutor exportExecutor = clazz.getDeclaredAnnotation(FileExportExecutor.class);

        Pair<String, String> fileUrl;
        boolean handleBreakFlag;
        GenerateFileResult genFileResult = null;
        try {
            HandleFileResult handleFileResult = handleFileWithRetry(executor, exportExecutor, baseRequest, registerId);
            genFileResult = handleFileResult.getGenFileResult();
            handleBreakFlag = handleFileResult.getGenFileResult().isHandleBreakFlag();
            fileUrl = handleFileResult.getFileUrlPair();
        } finally {
            // 上传完成时执行文件删除操作
            if (Objects.nonNull(genFileResult)) {
                genFileResult.destroy(logger);
            }
        }

        UploadCallbackRequest request = new UploadCallbackRequest();
        request.setRegisterId(registerId);
        request.setSystem(fileUrl.getKey());
        if (!handleBreakFlag) {
            request.setUploadStatus(UploadStatusEnum.SUCCESS);
            request.setFileUrl(fileUrl.getValue());
        } else {
            request.setUploadStatus(UploadStatusEnum.FAIL);
            Optional.ofNullable(genFileResult).ifPresent(k -> request.setErrorMsg(lessErrorMsg(k.getErrorMsg())));
        }
        downloadStorageService.uploadCallback(request);
        // 上传完成结果进行回调触发。
        ExportResult exportResult = buildExportResult(request);
        executor.asyncCompleteCallback(exportResult, baseRequest);
        return exportResult;
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
                AssertUtil.isTrue(mkdirs, GenerateFileErrorCode.CREATE_LOCAL_TEMP_FILE_ERROR);
            } catch (Exception ex) {
                logger.error(
                    "[AbstractAsyncFileHandlerAdapter#handle] create dictionary error,registerId:{},downloadCode:{}",
                    registerId, exportExecutor.value(), ex);
                handleBreakFlag = true;
                errorMsgJoiner.add(ex.getMessage());
            }
        }
        if (!file.exists()) {
            try {
                boolean fileHasName = file.createNewFile();
                AssertUtil.isTrue(fileHasName, GenerateFileErrorCode.FILE_NAME_DUPLICATE_ERROR,
                    GenerateFileException.class);
            } catch (Exception ex) {
                logger.error(
                    "[AbstractAsyncFileHandlerAdapter#handle] handle,create new file error,registerId:{},downloadCode:{},path:{}",
                    registerId, exportExecutor.value(), downloadProperties.getLocalFileTempPath(), ex);
                errorMsgJoiner.add("创建新文件异常,文件路径:" + downloadProperties.getLocalFileTempPath());
                handleBreakFlag = true;
            }
        }
        if (!handleBreakFlag) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                DownloaderRequestContext requestContext = buildRequestDownloaderRequest(out, baseRequest);
                executor.export(requestContext);
            } catch (Exception exception) {
                logger.error("[AbstractAsyncFileHandlerAdapter#handle] execute file error,downloadCode:{}",
                    exportExecutor.value(),
                    exception);
                errorMsgJoiner.add("导出文件逻辑异常:" + exception.getMessage());
                handleBreakFlag = true;
            }
        }
        // 执行文件压缩
        Pair<Boolean, File> compressResult = compress(file, handleBreakFlag);
        compress = compressResult.getKey();
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
        if (file.exists() && file.isFile() && !isZipCompress(file)) {
            if (downloadProperties.getMinEnableCompressMbSize() <= 0
                || file.length() >= downloadProperties.getMinEnableCompressMbSize()) {
                // 直接执行压缩
                // 获取文件的绝对路径
                try {
                    String path = file.getAbsolutePath();
                    CompressUtils.zip(path, path + FileSuffixEnum.ZIP.getFullFileSuffix());
                    return Pair.of(Boolean.TRUE, new File(path + FileSuffixEnum.ZIP.getFullFileSuffix()));
                } catch (Exception ex) {
                    logger
                        .warn("[AsyncFileHandleAdapter#compress] compress file fail! path:{}", file.getAbsolutePath());
                }
            }
        }
        return Pair.of(false, null);
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
        try {
            return retryer.call(() -> {
                // 生成文件
                GenerateFileResult genFileResult = generateFile(executor, exportExecutor, baseRequest, registerId,
                    downloadProperties.getLocalFileTempPath());
                Pair<String, String> fileUrl = null;
                Throwable error = null;
                if (!genFileResult.isHandleBreakFlag()) {
                    try {
                        String cnFileName = genCnFileName(baseRequest.getFileSuffix(), exportExecutor,
                            genFileResult.isCompress());
                        fileUrl = uploadService
                            .upload(genFileResult.getUploadFile(), cnFileName, downloadProperties.getAppId());
                    } catch (GenerateFileException ex) {
                        throw ex;
                    } catch (Throwable t) {
                        genFileResult.setHandleBreakFlag(true);
                        genFileResult.getErrorMsg().add("[文件上传]" + t.getMessage());
                        error = t;
                    }
                }
                return new HandleFileResult(genFileResult, fileUrl, error);
            });
        } catch (Exception e) {
            logger.error(
                "[AsyncFileHandlerAdapter#handleFileWithRetry] retry execute handle file,error!registerId:{},executor:{}",
                registerId, exportExecutor.value(), e);
            throw new EasyFileException(DownloadErrorCode.HANDLE_DOWNLOAD_FILE_ERROR);
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
