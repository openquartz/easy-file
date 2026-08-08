package com.openquartz.easyfile.storage.local.entity;

import lombok.Data;

/**
 * AsyncImportAppEntity
 *
 * @author svnee
 */
@Data
public class AsyncImportAppEntity {

    /**
     * 归属系统 APP ID
     */
    private String appId;

    /**
     * 统一 APP ID
     */
    private String unifiedAppId;

}
