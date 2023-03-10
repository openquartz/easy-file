package org.svnee.easyfile.server.service.impl;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.svnee.easyfile.admin.service.ServerAppIdProvider;

/**
 * request server id provider
 *
 * @author svnee
 **/
@Component
public class RequestServerIdProviderImpl implements ServerAppIdProvider {

    @Override
    public String getCurrentUnifiedAppId() {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getParameter("unifiedAppId");
    }

    @Override
    public boolean isShow() {
        return true;
    }
}
