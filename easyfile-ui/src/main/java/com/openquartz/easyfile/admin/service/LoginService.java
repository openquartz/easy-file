package com.openquartz.easyfile.admin.service;

import com.openquartz.easyfile.admin.constants.AdminConstants;
import com.openquartz.easyfile.admin.exception.LoginErrorCode;
import com.openquartz.easyfile.admin.model.EasyFileAdminUser;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.utils.CookieUtil;
import java.math.BigInteger;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.StringUtils;

/**
 * LoginService
 *
 * @author svnee
 * @since 1.2.0
 */
public class LoginService {

    private final AdminProperty adminProperty;

    public LoginService(AdminProperty adminProperty) {
        this.adminProperty = adminProperty;
    }

    private String makeToken(EasyFileAdminUser easyFileAdminUser) {
        String tokenJson = JSONUtil.toJson(easyFileAdminUser);
        return new BigInteger(tokenJson.getBytes()).toString(16);
    }

    private EasyFileAdminUser parseToken(String tokenHex) {
        EasyFileAdminUser easyFileAdminUser = null;
        if (tokenHex != null) {
            // format username_password(md5)
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());
            easyFileAdminUser = JSONUtil.parseObject(tokenJson, EasyFileAdminUser.class);
        }
        return easyFileAdminUser;
    }

    public ResponseResult<String> login(HttpServletResponse response, String username, String password,
        boolean rememberMe) {

        // param
        if (username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0) {
            return ResponseResult.fail(LoginErrorCode.LOGIN_PARAM_EMPTY_ERROR);
        }

        EasyFileAdminUser easyFileAdminUser = null;
        if (Objects.equals(username, adminProperty.getAdminUsername())
            && Objects.equals(password, adminProperty.getAdminPassword())) {
            easyFileAdminUser = new EasyFileAdminUser(adminProperty.getAdminUsername(),
                adminProperty.getAdminPassword());
        }
        if (easyFileAdminUser == null) {
            return ResponseResult.fail(LoginErrorCode.LOGIN_USERNAME_OR_PASSWORD_ERROR);
        }

        String loginToken = makeToken(easyFileAdminUser);

        // save login cookie
        CookieUtil.save(response, AdminConstants.LOGIN_IDENTITY_KEY, loginToken, rememberMe);
        return ResponseResult.ok();
    }

    /**
     * logout
     *
     * @param request request
     * @param response response
     */
    public ResponseResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, AdminConstants.LOGIN_IDENTITY_KEY);
        return ResponseResult.ok();
    }

    /**
     * logout
     */
    public EasyFileAdminUser ifLogin(HttpServletRequest request, HttpServletResponse response) {
        String cookieToken = CookieUtil.getValue(request, AdminConstants.LOGIN_IDENTITY_KEY);
        if (cookieToken != null) {
            EasyFileAdminUser cookieUser = null;
            try {
                cookieUser = parseToken(cookieToken);
            } catch (Exception e) {
                logout(request, response);
            }
            if (cookieUser != null) {
                EasyFileAdminUser dbUser = getByUsername(cookieUser.getUsername());
                if (dbUser != null && Objects.equals(cookieUser.getPassword(), dbUser.getPassword())) {
                    return dbUser;
                }
            }
        }
        return null;
    }

    private EasyFileAdminUser getByUsername(String username) {
        if (StringUtils.isNotBlank(username) && Objects.equals(username, adminProperty.getAdminUsername())) {
            return new EasyFileAdminUser(adminProperty.getAdminUsername(), adminProperty.getAdminPassword());
        }
        return null;
    }

}
