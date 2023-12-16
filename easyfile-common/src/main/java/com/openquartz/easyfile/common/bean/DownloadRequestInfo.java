package com.openquartz.easyfile.common.bean;

import lombok.Data;

/**
 * 下载请求Info
 *
 * @author svnee
 **/
@Data
public class DownloadRequestInfo {

    /**
     * 下载码
     */
    private String downloadCode;

    /**
     * 请求上下文
     */
    private IRequest requestContext;

}
