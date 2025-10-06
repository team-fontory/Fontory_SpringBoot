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
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
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
        log.info("Initializing JWT token provider");
        
        this.accessSecretKey = getSigningKey(props.getAccessSecretKey());
        this.refreshSecretKey = getSigningKey(props.getRefreshSecretKey());
        this.provideSecretKey = getSigningKey(props.getProvideSecretKey());
        SecretKey fontCreateSecretKey = getSigningKey(props.getFontCreateServerSecretKey());

        this.accessJwtParser = Jwts.parserBuilder().setSigningKey(accessSecretKey).build();
        this.refreshJwtParser = Jwts.parserBuilder().setSigningKey(refreshSecretKey).build();
        this.provideJwtParser = Jwts.parserBuilder().setSigningKey(provideSecretKey).build();
        this.fontCreateJwtParser = Jwts.parserBuilder().setSigningKey(fontCreateSecretKey).build();
        this.props = props;
        
        log.debug("JWT token provider initialized with token validities - access: {}ms, refresh: {}ms", 
                props.getAccessTokenValidityMs(), props.getRefreshTokenValidityMs());
    }

    public String generateTemporalProvideToken(String id) {
        log.debug("Generating temporal provide token for id: {}", id);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getTempTokenValidityMs());
        String token = Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(this.provideSecretKey)
                .compact();
        
        log.info("Temporal provide token generated: id={}, expiresAt={}", id, expiryDate);
        return token;
    }

    public Long getProvideId(String token) {
        log.debug("Extracting provide ID from token");
        
        Claims claims = provideJwtParser
                .parseClaimsJws(token)
                .getBody();
        Long provideId = Long.valueOf(claims.getSubject());
        
        log.debug("Provide ID extracted: {}", provideId);
        return provideId;
    }

    public String generateAccessToken(UserPrincipal user) {
        log.debug("Generating access token for user: {}", user.getId());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getAccessTokenValidityMs());
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(this.accessSecretKey)
                .compact();
        
        log.info("Access token generated: userId={}, expiresAt={}", user.getId(), expiryDate);
        return token;
    }

    public String generateRefreshToken(UserPrincipal user) {
        log.debug("Generating refresh token for user: {}", user.getId());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + props.getRefreshTokenValidityMs());
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshSecretKey)
                .compact();
        
        log.info("Refresh token generated: userId={}, expiresAt={}", user.getId(), expiryDate);
        return token;
    }

    public Long getMemberIdFromAccessToken(String token) {
        log.debug("Extracting member ID from access token");
        
        Claims claims = accessJwtParser
                .parseClaimsJws(token)
                .getBody();
        Long memberId = Long.valueOf(claims.getSubject());
        
        log.debug("Member ID extracted from access token: {}", memberId);
        return memberId;
    }

    public Long getMemberIdFromRefreshToken(String token) {
        log.debug("Extracting member ID from refresh token");
        
        Claims claims = refreshJwtParser
                .parseClaimsJws(token)
                .getBody();
        Long memberId = Long.valueOf(claims.getSubject());
        
        log.debug("Member ID extracted from refresh token: {}", memberId);
        return memberId;
    }

    public Authentication getAuthenticationFromAccessToken(String token) {
        log.debug("Creating authentication from access token");
        
        Claims claims = accessJwtParser
                .parseClaimsJws(token)
                .getBody();
        Long id = Long.valueOf(claims.getSubject());
        UserPrincipal principal = new UserPrincipal(id);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
        
        log.info("Authentication created from access token: userId={}, authorities={}", 
                id, principal.getAuthorities());
        return auth;
    }

    public String getFontCreateServer(String token) {
        log.debug("Validating font create server token");
        
        Claims claims = fontCreateJwtParser
                .parseClaimsJws(token)
                .getBody();
        String subject = claims.getSubject();
        
        log.debug("Font create server token validated: subject={}", subject);
        return subject;
    }
}
