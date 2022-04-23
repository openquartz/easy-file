package org.svnee.easyfile.server.service.executor;

import org.svnee.easyfile.server.entity.AsyncDownloadRecord;

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
     * @param downloadRecord 下载记录
     * @return 文件路径
     */
    String transform(AsyncDownloadRecord downloadRecord);

}
