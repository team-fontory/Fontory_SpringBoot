package org.fontory.fontorybe.authentication.adapter.inbound.security;

import static org.fontory.fontorybe.authentication.application.AuthConstants.ACCESS_TOKEN_COOKIE_NAME;
import static org.fontory.fontorybe.authentication.application.AuthConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.io.IOException;
import java.util.Optional;

import lombok.NonNull;
import org.fontory.fontorybe.authentication.adapter.outbound.exception.JwtAuthenticationException;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        log.info("JwtAuthenticationFilter: {} {}", req.getMethod(), req.getRequestURI());

        String accessToken = cookieUtils
                .extractTokenFromCookieInRequest(req, ACCESS_TOKEN_COOKIE_NAME)
                .or(() -> Optional.ofNullable(req.getHeader("Authorization"))
                        .map(h -> h.replaceFirst("^Bearer\\s+", "")))
                .orElse(null);
        String refreshToken = cookieUtils
                .extractTokenFromCookieInRequest(req, REFRESH_TOKEN_COOKIE_NAME)
                .orElse(null);
        log.info("Access token: {}, Refresh token: {}", accessToken, refreshToken);

        if (accessToken != null) {
            log.debug("Access token found - attempting authentication");
            authenticateOrRefresh(accessToken, refreshToken, res);
        } else if (refreshToken != null) {
            log.debug("Only refresh token found - attempting token reissue");
            tryRefresh(refreshToken, res);
        } else {
            log.debug("No tokens found → skip authentication");
        }

        log.debug("Proceeding with filter chain");
        chain.doFilter(req, res);

        log.debug("JwtAuthenticationFilter completed");
    }

    private void authenticateOrRefresh(String accessToken,
                                          String refreshToken,
                                   HttpServletResponse res) throws JwtAuthenticationException {
        try {
            log.debug("Validating access token");
            Authentication auth = jwtTokenProvider.getAuthenticationFromAccessToken(accessToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Authentication successful: user={}, authorities={}", auth.getName(), auth.getAuthorities());
        } catch (ExpiredJwtException e) {
            log.warn("Access token expired: {}", e.getMessage());
            if (refreshToken == null) {
                throw new JwtAuthenticationException("Refresh token missing");
            }
            log.debug("Attempting refresh with available refresh token");
            tryRefresh(refreshToken, res);
        } catch (JwtException ex) {
            log.error("Invalid access token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid access token", ex);
        }
    }

    private void tryRefresh(String refreshToken, HttpServletResponse res) throws JwtAuthenticationException {
        Long memberId;
        try {
            // refresh 토큰 검증 (Signature, 만료 등)
            log.debug("Validating refresh token");
            memberId = jwtTokenProvider.getMemberIdFromRefreshToken(refreshToken);
        } catch (ExpiredJwtException ex) {
            log.warn("Refresh token expired: {}", ex.getMessage());
            throw new JwtAuthenticationException("Refresh token expired", ex);
        } catch (JwtException ex) {
            log.warn("Invalid refresh token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid refresh token", ex);
        }

        // 검증 성공 -> 새 access/refresh 재발급
        log.info("Refresh token valid - issuing new token pair for user ID={}", memberId);
        ResponseCookies newCookies;
        try {
            newCookies = authService.refreshAuthCookies(memberId, refreshToken);
        } catch (InvalidRefreshTokenException e) {
            log.warn("Invalid refresh mismatch: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid refresh mismatch", e);
        }

        cookieUtils.addCookies(res, newCookies);

        String newAccess = newCookies.getAccessTokenCookie().getValue();
        Authentication auth = jwtTokenProvider.getAuthenticationFromAccessToken(newAccess);
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("Token reissue successful - user authenticated");
    }
}
