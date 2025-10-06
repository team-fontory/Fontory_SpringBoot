package org.fontory.fontorybe.authentication.application;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.application.dto.TokenResponse;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.stereotype.Service;

@Slf4j
@Builder
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberLookupService memberLookupService;
    private final CookieUtils cookieUtils;
    private final TokenStorage tokenStorage;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 새롭게 토큰 발급
     * 기존에 토큰이 존재한다면 제거, 기존 토큰이 존재할 필요 X
     */
    private TokenResponse issueNewTokens(Member member) {
        log.info("Issuing new token pair for member: memberId={}, provideId={}",
                member.getId(), member.getProvideId());
        
        UserPrincipal user = UserPrincipal.from(member);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        tokenStorage.saveRefreshToken(member, refreshToken);
        log.debug("Refresh token stored in Redis for member: memberId={}", member.getId());

        return TokenResponse.from(accessToken, refreshToken);
    }

    /**
     * 쿠키 발급
     */
    public ResponseCookies issueAuthCookies(Member member) {
        log.info("Creating auth cookies for member: memberId={}, status={}", 
                member.getId(), member.getStatus());
        
        TokenResponse tokenResponse = issueNewTokens(member);
        ResponseCookies cookies = ResponseCookies.from(
                cookieUtils.createAccessTokenCookie(tokenResponse.getAccessToken()),
                cookieUtils.createRefreshTokenCookie(tokenResponse.getRefreshToken())
        );
        
        log.debug("Auth cookies created successfully for member: memberId={}", member.getId());
        return cookies;
    }

    /**
     * 쿠키 재발급
     */
    public ResponseCookies refreshAuthCookies(Long memberId, String providedRefreshToken) {
        log.info("Refreshing auth cookies for member: memberId={}", memberId);
        
        Member member = memberLookupService.getOrThrowById(memberId);
        String storedRefreshToken = tokenStorage.getRefreshToken(member);

        if (storedRefreshToken == null) {
            log.warn("No stored refresh token found for member: memberId={}", memberId);
            throw new InvalidRefreshTokenException();
        }
        
        if (!storedRefreshToken.equals(providedRefreshToken)) {
            log.warn("Refresh token mismatch for member: memberId={}", memberId);
            throw new InvalidRefreshTokenException();
        }
        
        log.info("Refresh token validated successfully, issuing new tokens: memberId={}", memberId);
        return issueAuthCookies(member);
    }

    public void clearAuthCookies(HttpServletResponse res, Long memberId) {
        log.info("Clearing auth cookies for member: memberId={}", memberId);
        
        Member member = memberLookupService.getOrThrowById(memberId);
        cookieUtils.clearAuthCookies(res);
        tokenStorage.removeRefreshToken(member);
        
        log.info("Auth cookies and refresh token cleared successfully: memberId={}", memberId);
    }
}
