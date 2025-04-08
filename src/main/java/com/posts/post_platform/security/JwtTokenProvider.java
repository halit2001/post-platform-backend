package com.posts.post_platform.security;

import com.posts.post_platform.repository.UserRepositories;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Autowired
    private UserRepositories userRepositories;

    @Value("${postapp.app.app_secret}")
    private String APP_SECRET;

    @Value("${postapp.app.expires_in}")
    private Long EXPIRES_IN;

    /**
     * This class provides utility methods for generating, parsing, and validating JWT tokens.
     * It supports creating JWT tokens with user details and authorities, extracting claims,
     * and checking the validity of the token.
     */
    public String generateJwtToken(Authentication auth) {
        JwtUserDetails userDetails = (JwtUserDetails) auth.getPrincipal();
        Date expireDate = new Date(new Date().getTime() + EXPIRES_IN);
        Collection<? extends GrantedAuthority> grantedAuthorities = auth.getAuthorities();
        String authorities = grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining("&"));
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, APP_SECRET)
                .compact();
    }

    /**
     * Extracts the claims (payload) from the JWT token.
     * @param token The JWT token.
     * @return The claims parsed from the token.
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(APP_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retrieves the username (subject) from the JWT token.
     * @param token The JWT token.
     * @return The username extracted from the token.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Retrieves the authorities associated with the token.
     * @param token The JWT token.
     * @return An array of authorities extracted from the token.
     */
    public String[] getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("authorities", String.class).split("&");
    }

    /**
     * Validates whether the given JWT token is valid by checking the user's existence and expiration.
     * @param token The JWT token.
     * @return True if the token is valid, otherwise false.
     */
    public boolean validateToken(String token) {
        try {
            String username = getUsernameFromToken(token);
            if(userRepositories.findUserByUsername(username).isEmpty()) return false;
            return !isTokenExpired(token);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Checks if the JWT token has expired by comparing the expiration date with the current date.
     * @param token The JWT token.
     * @return True if the token is expired, otherwise false.
     */
    private boolean isTokenExpired(String token) {
        Date date = getClaims(token).getExpiration();
        return date.before(new Date());
    }
}
