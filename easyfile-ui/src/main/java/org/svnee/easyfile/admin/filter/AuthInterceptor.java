package org.svnee.easyfile.admin.filter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.svnee.easyfile.admin.constants.AdminConstants;
import org.svnee.easyfile.admin.controller.annotation.Auth;
import org.svnee.easyfile.admin.model.EasyFileAdminUser;
import org.svnee.easyfile.admin.service.LoginService;

/**
 * 权限拦截
 *
 * @author svnee
 */
@Component
public class AuthInterceptor implements AsyncHandlerInterceptor {

    @Resource
    private LoginService loginService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        Auth auth = method.getMethodAnnotation(Auth.class);
        if (auth != null && auth.needLogin()) {
            EasyFileAdminUser loginUser = loginService.ifLogin(request, response);
            if (loginUser == null) {
                response.setStatus(302);
                response.setHeader("location", request.getContextPath() + "/easyfile-ui/toLogin");
                return false;
            }
            request.setAttribute(AdminConstants.LOGIN_IDENTITY_KEY, loginUser);
        }

        return true;
    }

}
