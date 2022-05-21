package org.svnee.easyfile.server.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.server.entity.UserInfo;
import org.svnee.easyfile.server.exception.AuthenticationExceptionCode;
import org.svnee.easyfile.server.service.UserService;

/**
 * User details service impl.
 *
 * @author svnee
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userService.getByUsername(username);

        Asserts.notNull(userInfo, AuthenticationExceptionCode.USER_NOT_EXISTED_ERROR);

        JwtUser jwtUser = new JwtUser();
        jwtUser.setId(userInfo.getId());
        jwtUser.setUsername(username);
        jwtUser.setPassword(userInfo.getPassword());

        Set<SimpleGrantedAuthority> authorities = Collections
            .singleton(new SimpleGrantedAuthority(userInfo.getRole() + ""));
        jwtUser.setAuthorities(authorities);

        return jwtUser;
    }

}
