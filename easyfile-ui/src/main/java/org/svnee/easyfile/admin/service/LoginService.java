package org.svnee.easyfile.admin.service;

import static org.svnee.easyfile.admin.constants.AdminConstants.LOGIN_IDENTITY_KEY;

import java.math.BigInteger;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.DigestUtils;
import org.svnee.easyfile.admin.exception.LoginErrorCode;
import org.svnee.easyfile.admin.model.EasyFileUser;
import org.svnee.easyfile.admin.property.AdminProperty;
import org.svnee.easyfile.admin.utils.CookieUtil;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * LoginService
 *
 * @author svnee
 */
public class LoginService {

    private final AdminProperty adminProperty;

    public LoginService(AdminProperty adminProperty) {
        this.adminProperty = adminProperty;
    }

    private String makeToken(EasyFileUser easyFileUser) {
        String tokenJson = JSONUtil.toJson(easyFileUser);
        return new BigInteger(tokenJson.getBytes()).toString(16);
    }

    private EasyFileUser parseToken(String tokenHex) {
        EasyFileUser easyFileUser = null;
        if (tokenHex != null) {
            // format username_password(md5)
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());
            easyFileUser = JSONUtil.parseObject(tokenJson, EasyFileUser.class);
        }
        return easyFileUser;
    }

    public ResponseResult<String> login(HttpServletResponse response, String username, String password,
        boolean rememberMe) {

        // param
        if (username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0) {
            return ResponseResult.fail(LoginErrorCode.LOGIN_PARAM_EMPTY_ERROR);
        }

        EasyFileUser easyFileUser = null;
        if (Objects.equals(username, adminProperty.getAdminUsername())
            && Objects.equals(password, adminProperty.getAdminPassword())) {
            easyFileUser = new EasyFileUser(adminProperty.getAdminUsername(), adminProperty.getAdminPassword());
        }
        if (easyFileUser == null) {
            return ResponseResult.fail(LoginErrorCode.LOGIN_PARAM_EMPTY_ERROR);
        }

        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passwordMd5.equals(easyFileUser.getPassword())) {
            return ResponseResult.fail(LoginErrorCode.LOGIN_PARAM_EMPTY_ERROR);
        }

        String loginToken = makeToken(easyFileUser);

        // save login cookie
        CookieUtil.save(response, LOGIN_IDENTITY_KEY, loginToken, rememberMe);
        return ResponseResult.ok();
    }

    /**
     * logout
     *
     * @param request request
     * @param response response
     */
    public ResponseResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
        return ResponseResult.ok();
    }

    /**
     * logout
     */
    public EasyFileUser ifLogin(HttpServletRequest request, HttpServletResponse response) {
        String cookieToken = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (cookieToken != null) {
            EasyFileUser cookieUser = null;
            try {
                cookieUser = parseToken(cookieToken);
            } catch (Exception e) {
                logout(request, response);
            }
            if (cookieUser != null) {
                EasyFileUser dbUser = getByUsername(cookieUser.getUsername());
                if (dbUser != null && Objects.equals(cookieUser.getPassword(), dbUser.getPassword())) {
                    return dbUser;
                }
            }
        }
        return null;
    }

    private EasyFileUser getByUsername(String username) {
        if (StringUtils.isNotBlank(username) && Objects.equals(username, adminProperty.getAdminUsername())) {
            return new EasyFileUser(adminProperty.getAdminUsername(), adminProperty.getAdminPassword());
        }
        return null;
    }

}
