package com.openquartz.easyfile.common.property;

/**
 * easyfile common properties
 *
 * @author svnee
 */
public interface IEasyFileCommonProperties {

    /**
     * 服务APPID
     * app-id
     *
     * @return appId
     */
    String getAppId();

    /**
     * 服务统一标识
     * unified-app-id 默认使用{@link #getAppId()}
     *
     * @return 服务统一标识
     */
    String getUnifiedAppId();

}
