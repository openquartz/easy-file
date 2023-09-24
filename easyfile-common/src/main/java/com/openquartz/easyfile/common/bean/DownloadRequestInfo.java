package com.openquartz.easyfile.common.bean;

import lombok.Data;

/**
 * 下载请求Info
 *
 * @author svnee
 **/
@Data
public class DownloadRequestInfo {

    private String downloadCode;
    private BaseExporterRequestContext requestContext;

}
