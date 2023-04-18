package com.openquartz.easyfile.admin.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openquartz.easyfile.common.constants.Constants;

/**
 * Cookie Utils
 *
 * @author svnee
 * @since 1.2.0
 */
public final class CookieUtil {

    private CookieUtil() {
    }

    /**
     * cookie default cache seconds
     */
    private static final int COOKIE_CACHE_MAX_SECONDS = 300;

    /**
     * cookie path
     */
    private static final String COOKIE_PATH = "/";

    /**
     * save cookie
     */
    public static void save(HttpServletResponse response, String key, String value, boolean rememberMe) {
        int age = !rememberMe ? COOKIE_CACHE_MAX_SECONDS : -1;
        save(response, key, value, age);
    }

    /**
     * save cookie
     */
    private static void save(HttpServletResponse response, String key, String value,
        int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath(CookieUtil.COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * get cookie value
     *
     * @param request request
     * @param key cookie key
     */
    public static String getValue(HttpServletRequest request, String key) {
        Cookie cookie = get(request, key);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /**
     * get cookie from request
     *
     * @param request request
     * @param key cookie key
     */
    private static Cookie get(HttpServletRequest request, String key) {
        Cookie[] cookieArr = request.getCookies();
        if (cookieArr != null && cookieArr.length > 0) {
            for (Cookie cookie : cookieArr) {
                if (cookie.getName().equals(key)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * remove cookie if exist
     */
    public static void remove(HttpServletRequest request, HttpServletResponse response, String key) {
        Cookie cookie = get(request, key);
        if (cookie != null) {
            save(response, key, Constants.EMPTY_STRING, 0);
        }
    }

}