package org.fontory.fontorybe.authentication.adapter.outbound;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

import static org.fontory.fontorybe.authentication.application.AuthConstants.*;

@Component
@RequiredArgsConstructor
public class CookieUtilsImpl implements CookieUtils {
    @Value("${cookies.domain:localhost}")
    private String cookieDomain;
    private final JwtProperties jwtProperties;

    // Generic builder
    public ResponseCookie createCookie(String name,
                                      String value,
                                      long maxAgeSeconds,
                                      String sameSite) {
        return ResponseCookie.from(name, value)
                .domain(cookieDomain)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite)
                .build();
    }

    // accessToken 15분
    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                token,
                jwtProperties.getAccessTokenValidityMs(),
                ACCESS_TOKEN_COOKIE_SAME_SITE
        );
    }

    // refreshToken 7일
    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                token,
                jwtProperties.getRefreshTokenValiditySec(),
                REFRESH_TOKEN_COOKIE_SAME_SITE
        );
    }

    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookies expiredCookies = ResponseCookies.from(createExpiredAccessCookie(), createExpiredRefreshCookie());
        addCookies(response, expiredCookies);
    }

    private ResponseCookie createExpiredAccessCookie() {
        return createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                "",
                0,
                ACCESS_TOKEN_COOKIE_SAME_SITE
        );
    }

    private ResponseCookie createExpiredRefreshCookie() {
        return createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                "",
                0,
                REFRESH_TOKEN_COOKIE_NAME
        );
    }

    public Optional<String> extractTokenFromCookieInRequest(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public void addCookies(HttpServletResponse response, ResponseCookies cookies) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.accessTokenCookieToString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookies.refreshTokenCookieToString());
    }
}
