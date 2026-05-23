package com.openquartz.easyfile.storage.file;

import java.io.File;

/**
 * 文件下载服务
 *
 * @author svnee
 */
public interface DownloadService {

    /**
     * 下载文件到本地
     *
     * @param fileUrl 文件地址
     * @param localFile 本地文件
     */
    void download(String fileUrl, File localFile);
}
