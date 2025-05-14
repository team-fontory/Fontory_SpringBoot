package org.fontory.fontorybe.authentication.adapter.outbound;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    final JwtProperties props;

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final SecretKey provideSecretKey;

    private final JwtParser accessJwtParser;
    private final JwtParser refreshJwtParser;
    private final JwtParser provideJwtParser;
    private final JwtParser fontCreateJwtParser;

    private SecretKey getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenProviderImpl(
            JwtProperties props) {
        this.accessSecretKey = getSigningKey(props.getAccessSecretKey());
        this.refreshSecretKey = getSigningKey(props.getRefreshSecretKey());
        this.provideSecretKey = getSigningKey(props.getProvideSecretKey());
        SecretKey fontCreateSecretKey = getSigningKey(props.getFontCreateServerSecretKey());

        this.accessJwtParser = Jwts.parserBuilder().setSigningKey(accessSecretKey).build();
        this.refreshJwtParser = Jwts.parserBuilder().setSigningKey(refreshSecretKey).build();
        this.provideJwtParser = Jwts.parserBuilder().setSigningKey(provideSecretKey).build();
        this.fontCreateJwtParser = Jwts.parserBuilder().setSigningKey(fontCreateSecretKey).build();
        this.props = props;
    }

    public String generateTemporalProvideToken(String id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getTempTokenValidityMs());
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(this.provideSecretKey)
                .compact();
    }

    public Long getProvideId(String token) {
        Claims claims = provideJwtParser
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    public String generateAccessToken(UserPrincipal user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getAccessTokenValidityMs());
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(this.accessSecretKey)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getRefreshTokenValidityMs());
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshSecretKey)
                .compact();
    }

    public Long getMemberIdFromAccessToken(String token) {
        Claims claims = accessJwtParser
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    public Long getMemberIdFromRefreshToken(String token) {
        Claims claims = refreshJwtParser
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    public Authentication getAuthenticationFromAccessToken(String token) {
        Claims claims = accessJwtParser
                .parseClaimsJws(token)
                .getBody();
        Long id = Long.valueOf(claims.getSubject());
        UserPrincipal principal = new UserPrincipal(id);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public String getFontCreateServer(String token) {
        Claims claims = fontCreateJwtParser
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
