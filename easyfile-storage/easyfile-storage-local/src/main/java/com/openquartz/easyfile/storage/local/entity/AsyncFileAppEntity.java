package com.openquartz.easyfile.storage.local.entity;

import lombok.Data;

/**
 * AsyncFileAppEntity
 *
 * @author svnee
 **/
@Data
public class AsyncFileAppEntity {

    /**
     * 归属系统 APP ID
     */
    private String appId;

    /**
     * 统一APP ID
     */
    private String unifiedAppId;

}
