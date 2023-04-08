package com.openquartz.easyfile.storage.local.entity;

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
