package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * 下载url 结果
 *
 * @author svnee
 * @since 1.2.1
 **/
@Data
public class DownloadUrlResult {

    private String url;
    private String fileName;
    private String fileSystem;
}
