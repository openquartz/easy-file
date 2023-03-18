package org.svnee.easyfile.core.executor.bean;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.StringJoiner;
import org.slf4j.Logger;

/**
 * 生成文件结果
 *
 * @author svnee
 */
public class GenerateFileResult {

    /**
     * 异常信息
     */
    private final StringJoiner errorMsg;

    /**
     * 生成文件
     */
    private final File genFile;

    /**
     * 处理中断标记
     */
    private boolean handleBreakFlag;

    /**
     * 是否执行了压缩文件
     */
    private boolean compress;

    /**
     * 压缩文件
     */
    private final File compressFile;

    public GenerateFileResult(StringJoiner errorMsg, File genFile, File compressFile, boolean handleBreakFlag,
        boolean compress) {
        this.errorMsg = errorMsg;
        this.genFile = genFile;
        this.compressFile = compressFile;
        this.handleBreakFlag = handleBreakFlag;
        this.compress = compress;
    }

    /**
     * 构建对象
     *
     * @param errorMsg 异常信息
     * @param genFile 生成文件
     * @param handleBreakFlag 处理中断标识
     * @return 生成文件结果
     */
    public static GenerateFileResult build(StringJoiner errorMsg, File genFile, File compressFile,
        boolean handleBreakFlag,
        boolean compress) {
        return new GenerateFileResult(errorMsg, genFile, compressFile, handleBreakFlag, compress);
    }

    public StringJoiner getErrorMsg() {
        return errorMsg;
    }

    public File getGenFile() {
        return genFile;
    }

    public boolean isHandleBreakFlag() {
        return handleBreakFlag;
    }

    public void setHandleBreakFlag(boolean handleBreakFlag) {
        this.handleBreakFlag = handleBreakFlag;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    /**
     * 获取需要上传的文件
     *
     * @return 如果开启了压缩则走压缩文件, 否则使用原文件
     */
    public File getUploadFile() {
        if (compress) {
            return compressFile;
        }
        return genFile;
    }

    /**
     * 注销时,删除文件
     * 删除原文件与压缩文件
     */
    public void destroy(Logger logger, boolean cleanFileAfterUpload) {
        try {
            // 上传完成时执行文件删除操作
            if (Objects.nonNull(genFile) && genFile.exists() && cleanFileAfterUpload) {
                Files.delete(genFile.toPath());
            }
        } catch (Exception exception) {
            logger.error("[AbstractAsyncFileHandlerAdapter#handle] delete file error", exception);
        }
        try {
            // 上传完成时执行文件删除操作
            if (Objects.nonNull(compressFile) && compressFile.exists()) {
                Files.delete(compressFile.toPath());
            }
        } catch (Exception exception) {
            logger.error("[AbstractAsyncFileHandlerAdapter#handle] delete compress file error", exception);
        }
    }
}
