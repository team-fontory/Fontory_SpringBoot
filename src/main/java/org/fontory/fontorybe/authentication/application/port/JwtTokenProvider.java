package org.fontory.fontorybe.authentication.application.port;

import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.springframework.security.core.Authentication;

public interface JwtTokenProvider {
    String generateAccessToken(UserPrincipal user);
    String generateRefreshToken(UserPrincipal user);
    String generateTemporalProvideToken(String id);
    Long getMemberIdFromAccessToken(String token);
    Long getMemberIdFromRefreshToken(String token);
    Long getProvideId(String token);
    Authentication getAuthenticationFromAccessToken(String token);
    String getFontCreateServer(String token);
}