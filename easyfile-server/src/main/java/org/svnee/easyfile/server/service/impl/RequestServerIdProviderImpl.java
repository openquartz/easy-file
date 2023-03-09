package org.svnee.easyfile.server.service.impl;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.svnee.easyfile.admin.service.ServerAppIdProvider;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;

/**
 * request server id provider
 *
 * @author svnee
 **/
@Component
public class RequestServerIdProviderImpl implements ServerAppIdProvider {

    @Override
    public String getCurrentAppId() {

        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String appId = request.getParameter("appId");
        Asserts.notEmpty(appId, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        return appId;
    }

    @Override
    public String getCurrentUnifiedAppId() {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String appId = request.getParameter("unifiedAppId");
        Asserts.notEmpty(appId, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        return appId;
    }

}
