package com.openquartz.easyfile.admin.service;

import com.openquartz.easyfile.common.property.IEasyFileCommonProperties;

/**
 * DefaultServiceAppIdProvider
 *
 * @author svnee
 **/
public class DefaultServiceAppIdProvider implements ServerAppIdProvider {

    private final IEasyFileCommonProperties commonProperties;

    public DefaultServiceAppIdProvider(IEasyFileCommonProperties commonProperties) {
        this.commonProperties = commonProperties;
    }

    @Override
    public String getCurrentUnifiedAppId() {
        return commonProperties.getUnifiedAppId();
    }
}
