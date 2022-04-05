package org.svnee.easyfile.server.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.server.exception.LoginExceptionCode;

import static org.svnee.easyfile.common.constants.Constants.ACCESS_TOKEN;

/**
 * JWT authorization filter.
 *
 * @author svnee
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtTokenManager tokenManager;

    public JWTAuthorizationFilter(JwtTokenManager tokenManager, AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.tokenManager = tokenManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        // 验证客户端交互时 Token
        String accessToken = request.getParameter(ACCESS_TOKEN);
        if (StringUtils.isNotBlank(accessToken)) {
            tokenManager.validateToken(accessToken);

            Authentication authentication = this.tokenManager.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
            return;
        }

        // 如果请求头中没有 Authorization 信息则直接放行
        String tokenHeader = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        if (tokenHeader == null || !tokenHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // 如果请求头中有 Token, 则进行解析, 并且设置认证信息
        try {
            SecurityContextHolder.getContext().setAuthentication(getAuthentication(tokenHeader));
        } catch (Exception ex) {
            // 返回 Json 形式的错误信息
            response.setCharacterEncoding(Constants.UTF_8);
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(JSONUtil.toJson(ResponseResult.fail("-1", ex.getMessage())));
            response.getWriter().flush();
            return;
        }

        try {
            super.doFilterInternal(request, response, chain);
        } finally {
            UserContext.clear();
        }
    }

    /**
     * Token 中获取用户信息并新建一个 Token.
     *
     * @param tokenHeader tokenHeader
     * @return token
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
        String token = tokenHeader.replace(JwtTokenUtil.TOKEN_PREFIX, StringUtils.EMPTY);
        boolean expiration = JwtTokenUtil.isExpiration(token);
        if (expiration) {
            throw new EasyFileException(LoginExceptionCode.LOGIN_EXPIRE_ERROR);
        }

        String username = JwtTokenUtil.getUsername(token);
        String userRole = JwtTokenUtil.getUserRole(token);
        UserContext.setUserInfo(username, userRole);

        String role = JwtTokenUtil.getUserRole(token);
        if (username != null) {
            return new UsernamePasswordAuthenticationToken(username, null,
                Collections.singleton(new SimpleGrantedAuthority(role))
            );
        }

        return null;
    }

}
