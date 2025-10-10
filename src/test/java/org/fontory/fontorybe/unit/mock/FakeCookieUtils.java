package org.fontory.fontorybe.unit.mock;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.springframework.http.ResponseCookie;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.fontory.fontorybe.authentication.application.AuthConstants.*;

public class FakeCookieUtils implements CookieUtils {
    private final Map<String, String> cookies = new HashMap<>();
    private boolean clearCookiesCalled = false;

    @Override
    public ResponseCookie createCookie(String name, String value, long maxAgeSeconds, String sameSite) {
        return ResponseCookie.from(name, value)
                .domain("localhost")
                .httpOnly(HTTP_ONLY)
                .secure(false) // for testing
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite)
                .build();
    }

    @Override
    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                token,
                900, // 15 minutes in seconds
                ACCESS_TOKEN_COOKIE_SAME_SITE
        );
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                token,
                604800, // 7 days in seconds
                REFRESH_TOKEN_COOKIE_SAME_SITE
        );
    }

    @Override
    public Optional<String> extractTokenFromCookieInRequest(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(cookies.get(cookieName));
    }

    @Override
    public void addCookies(HttpServletResponse response, ResponseCookies cookies) {
        this.cookies.put(ACCESS_TOKEN_COOKIE_NAME, cookies.getAccessTokenCookie().getValue());
        this.cookies.put(REFRESH_TOKEN_COOKIE_NAME, cookies.getRefreshTokenCookie().getValue());
    }

    @Override
    public void clearAuthCookies(HttpServletResponse response) {
        cookies.remove(ACCESS_TOKEN_COOKIE_NAME);
        cookies.remove(REFRESH_TOKEN_COOKIE_NAME);
        clearCookiesCalled = true;
    }

    // Test helper methods
    public boolean isClearCookiesCalled() {
        return clearCookiesCalled;
    }

    public void resetClearCookiesFlag() {
        clearCookiesCalled = false;
    }

    public Map<String, String> getCookies() {
        return new HashMap<>(cookies);
    }

    public void reset() {
        cookies.clear();
        clearCookiesCalled = false;
    }
}