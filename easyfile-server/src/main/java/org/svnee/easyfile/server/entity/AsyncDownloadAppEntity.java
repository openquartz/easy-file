package org.svnee.easyfile.server.entity;

import lombok.Data;

/**
 * AsyncDownloadAppEntity
 *
 * @author svnee
 **/
@Data
public class AsyncDownloadAppEntity {

    /**
     * 归属系统 APP ID
     */
    private String appId;

    /**
     * 统一APP ID
     */
    private String unifiedAppId;

}
