package org.svnee.easyfile.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.server.exception.AuthenticationExceptionCode;

/**
 * JWT authentication filter.
 *
 * @author svnee
 */
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private static final ThreadLocal<Integer> REMEMBER_ME = new NamedThreadLocal<>("RememberMe");

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        super.setFilterProcessesUrl(Constants.BASE_PATH + "/auth/users/apply/token");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        // 从输入流中获取到登录的信息
        try {
            LoginUser loginUser = new ObjectMapper().readValue(request.getInputStream(), LoginUser.class);
            REMEMBER_ME.set(loginUser.getRememberMe());
            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword(),
                    new ArrayList<>())
            );
        } catch (IOException e) {
            logger.error("attemptAuthentication error :{}", e);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain,
        Authentication authResult) throws IOException {
        JwtUser jwtUser = (JwtUser) authResult.getPrincipal();
        boolean isRemember = REMEMBER_ME.get() == 1;

        String role = "";
        Collection<? extends GrantedAuthority> authorities = jwtUser.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            role = authority.getAuthority();
        }

        String token = JwtTokenUtil.createToken(jwtUser.getId(), jwtUser.getUsername(), role, isRemember);
        response.setHeader("token", JwtTokenUtil.TOKEN_PREFIX + token);
        response.setCharacterEncoding(Constants.UTF_8);
        Map<String, Object> maps = new HashMap<>();
        maps.put("data", JwtTokenUtil.TOKEN_PREFIX + token);
        response.getWriter().write(JSONUtil.toJson(ResponseResult.ok(maps)));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException failed) throws IOException {
        response.setCharacterEncoding(Constants.UTF_8);
        response.getWriter().write(JSONUtil.toJson(ResponseResult.fail(AuthenticationExceptionCode.AUTH_FAILED_ERROR
            .getErrorCode(), AuthenticationExceptionCode.AUTH_FAILED_ERROR.getErrorMsg())));
    }

}
