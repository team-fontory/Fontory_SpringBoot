package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeJwtTokenProvider implements JwtTokenProvider {
    private final Map<String, Long> accessTokenToMemberId = new HashMap<>();
    private final Map<String, Long> refreshTokenToMemberId = new HashMap<>();
    private final Map<String, Long> temporalTokenToProvideId = new HashMap<>();
    private final Map<String, String> temporalTokenToFontServer = new HashMap<>();

    @Override
    public String generateAccessToken(UserPrincipal user) {
        String token = "access_" + UUID.randomUUID().toString();
        accessTokenToMemberId.put(token, user.getId());
        return token;
    }

    @Override
    public String generateRefreshToken(UserPrincipal user) {
        String token = "refresh_" + UUID.randomUUID().toString();
        refreshTokenToMemberId.put(token, user.getId());
        return token;
    }

    @Override
    public String generateTemporalProvideToken(String id) {
        String token = "temporal_" + UUID.randomUUID().toString();
        temporalTokenToProvideId.put(token, Long.valueOf(id));
        return token;
    }

    @Override
    public Long getMemberIdFromAccessToken(String token) {
        return accessTokenToMemberId.get(token);
    }

    @Override
    public Long getMemberIdFromRefreshToken(String token) {
        return refreshTokenToMemberId.get(token);
    }

    @Override
    public Long getProvideId(String token) {
        return temporalTokenToProvideId.get(token);
    }

    @Override
    public Authentication getAuthenticationFromAccessToken(String token) {
        // For unit tests, we can return a mock authentication
        Long memberId = accessTokenToMemberId.get(token);
        if (memberId == null) {
            return null;
        }
        // Return a simple mock authentication for testing
        return new MockAuthentication(memberId);
    }

    @Override
    public String getFontCreateServer(String token) {
        return temporalTokenToFontServer.get(token);
    }

    // Test helper methods
    public void reset() {
        accessTokenToMemberId.clear();
        refreshTokenToMemberId.clear();
        temporalTokenToProvideId.clear();
        temporalTokenToFontServer.clear();
    }

    public boolean isValidAccessToken(String token) {
        return accessTokenToMemberId.containsKey(token);
    }

    public boolean isValidRefreshToken(String token) {
        return refreshTokenToMemberId.containsKey(token);
    }

    // Simple mock authentication class for testing
    private static class MockAuthentication implements Authentication {
        private final Long memberId;

        public MockAuthentication(Long memberId) {
            this.memberId = memberId;
        }

        @Override
        public String getName() {
            return memberId.toString();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return memberId;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // No-op for test
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }
    }
}