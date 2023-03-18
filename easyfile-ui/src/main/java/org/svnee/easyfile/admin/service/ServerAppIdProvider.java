package org.svnee.easyfile.admin.service;

/**
 * Server AppId Provider
 *
 * @author svnee
 */
public interface ServerAppIdProvider {

    /**
     * 服务统一标识
     * unified-app-id
     *
     * @return 服务统一标识
     */
    String getCurrentUnifiedAppId();

    /**
     * 是否展示用于筛选
     *
     * @return show flag
     */
    default boolean isShow() {
        return false;
    }

}
