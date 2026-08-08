package com.openquartz.easyfile.storage.file.url;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.storage.file.DownloadService;

/**
 * 基于URL的默认文件下载服务
 *
 * @author svnee
 */
@Slf4j
public class UrlDownloadServiceImpl implements DownloadService {

    @Override
    public void download(String fileUrl, File localFile) {
        try {
            URL url = new URL(fileUrl);
            String protocol = url.getProtocol();
            if (protocol == null
                || (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol))) {
                throw new IllegalArgumentException("仅支持 http/https 协议下载: " + fileUrl);
            }
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("文件下载成功: {} -> {}", fileUrl, localFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + fileUrl, e);
        }
    }
}
