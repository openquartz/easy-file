package org.svnee.easyfile.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.server.bean.constants.AuthConstants;


/**
 * Jwt token manager.
 *
 * @author svnee
 */
@Component
public class JwtTokenManager {

    public String createToken(String userName) {
        long now = System.currentTimeMillis();

        Date validity;
        validity = new Date(now + AuthConstants.TOKEN_VALIDITY_IN_SECONDS * 1000L);

        Claims claims = Jwts.claims().setSubject(userName);
        return Jwts.builder().setClaims(claims).setExpiration(validity)
            .signWith(SignatureAlgorithm.HS512, AuthConstants.SECRET).compact();
    }

    public void validateToken(String token) {
        Jwts.parser().setSigningKey(AuthConstants.SECRET).parseClaimsJws(token);
    }

    /**
     * Get auth Info.
     *
     * @param token token
     * @return auth info
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(AuthConstants.SECRET).parseClaimsJws(token).getBody();

        List<GrantedAuthority> authorities = AuthorityUtils
            .commaSeparatedStringToAuthorityList((String) claims.get(AuthConstants.AUTHORITIES_KEY));

        User principal = new User(claims.getSubject(), StringUtils.EMPTY, authorities);
        return new UsernamePasswordAuthenticationToken(principal, StringUtils.EMPTY, authorities);
    }

}
