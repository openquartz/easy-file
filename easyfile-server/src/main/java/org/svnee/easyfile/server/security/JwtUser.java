package org.svnee.easyfile.server.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Jwt user.
 *
 * @author svnee
 */
@Data
public class JwtUser implements UserDetails {

    /**
     * id
     */
    private Long id;

    /**
     * userName
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * authorities
     */
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
