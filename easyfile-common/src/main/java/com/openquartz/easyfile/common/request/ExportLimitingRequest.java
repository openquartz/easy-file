package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.bean.BaseExecuteParam;

/**
 * 导出限流请求
 *
 * @author svnee
 */
public class ExportLimitingRequest extends BaseExecuteParam {

    /**
     * 服务ID
     */
    private String appId;

    /**
     * 当前机器 IP地址
     */
    private String ipAddr;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
}

