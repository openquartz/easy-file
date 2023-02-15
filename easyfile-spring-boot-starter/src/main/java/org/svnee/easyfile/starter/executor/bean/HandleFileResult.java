package org.svnee.easyfile.starter.executor.bean;

import lombok.Data;
import org.svnee.easyfile.common.bean.Pair;

/**
 * 处理文件结果
 *
 * @author svnee
 */
@Data
public class HandleFileResult {

    /**
     * 生成文件结果
     */
    private GenerateFileResult genFileResult;

    /**
     * 文件上传结果路径
     */
    private Pair<String, String> fileUrlPair;

    /**
     * local generate file name
     */
    private String localFileName;

    /**
     * 异常信息
     */
    private Throwable ex;

    public HandleFileResult(GenerateFileResult genFileResult, Pair<String, String> fileUrlPair,
        Throwable ex) {
        this.genFileResult = genFileResult;
        this.fileUrlPair = fileUrlPair;
        this.ex = ex;
    }

    public HandleFileResult localFileName(String localFileName) {
        this.localFileName = localFileName;
        return this;
    }
}
