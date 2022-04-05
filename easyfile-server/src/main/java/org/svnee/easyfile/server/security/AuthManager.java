package org.svnee.easyfile.server.security;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.expression.AccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Auth manager
 *
 * @author svnee
 */
@Component
@AllArgsConstructor
public class AuthManager {

    private final JwtTokenManager jwtTokenManager;
    private final AuthenticationManager authenticationManager;

    /**
     * Resolve token from user.
     *
     * @param username username
     * @param rawPassword password
     * @return token
     */
    @SneakyThrows
    public String resolveTokenFromUser(String username, String rawPassword) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
            authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new AccessException("Unknown user.");
        }

        return jwtTokenManager.createToken(username);
    }

}
