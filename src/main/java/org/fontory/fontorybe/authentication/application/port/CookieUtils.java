package org.fontory.fontorybe.authentication.application.port;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

public interface CookieUtils {
    ResponseCookie createCookie(String name, String value, long maxAgeSeconds, String sameSite);
    ResponseCookie createAccessTokenCookie(String token);
    ResponseCookie createRefreshTokenCookie(String token);
    Optional<String> extractTokenFromCookieInRequest(HttpServletRequest request, String cookieName);
    void addCookies(HttpServletResponse response, ResponseCookies cookies);
    void clearAuthCookies(HttpServletResponse response);
}
