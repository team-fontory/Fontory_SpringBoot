package org.fontory.fontorybe.authentication.adapter.outbound;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.*;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@NoArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String SECRET_KEY;

    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final long TEMP_TOKEN_VALIDITY = 15 * 60 * 1000; // 10 minutes

    public JwtTokenProvider(String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    public String generateTemporalProvideToken(String id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TEMP_TOKEN_VALIDITY);
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public Long getProvideId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    public String generateAccessToken(UserPrincipal user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public Long getMemberId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        Long id = Long.valueOf(claims.getSubject());
        UserPrincipal principal = new UserPrincipal(id);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}
