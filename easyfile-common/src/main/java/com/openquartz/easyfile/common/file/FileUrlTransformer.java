package com.openquartz.easyfile.common.file;

/**
 * 文件转存
 *
 * @author svnee
 */
public interface FileUrlTransformer {

    /**
     * 文件上传系统
     *
     * @return 文件系统
     */
    String fileSystem();

    /**
     * 转换到文件存储器
     *
     * @param fileKey 文件key
     * @return 文件路径
     */
    String transform(String fileKey);

}
