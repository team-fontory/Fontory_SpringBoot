package org.fontory.fontorybe.authentication.application;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
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
        UserPrincipal user = UserPrincipal.from(member);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        tokenStorage.saveRefreshToken(member, refreshToken);

        return TokenResponse.from(accessToken, refreshToken);
    }

    /**
     * 쿠키 발급
     */
    public ResponseCookies issueAuthCookies(Member member) {
        TokenResponse tokenResponse = issueNewTokens(member);
        return ResponseCookies.from(
                cookieUtils.createAccessTokenCookie(tokenResponse.getAccessToken()),
                cookieUtils.createRefreshTokenCookie(tokenResponse.getRefreshToken())
        );
    }

    /**
     * 쿠키 재발급
     */
    public ResponseCookies refreshAuthCookies(Long memberId, String providedRefreshToken) {
        Member member = memberLookupService.getOrThrowById(memberId);
        String storedRefreshToken = tokenStorage.getRefreshToken(member);

        if (storedRefreshToken == null || !storedRefreshToken.equals(providedRefreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        return issueAuthCookies(member);
    }

    public void clearAuthCookies(HttpServletResponse res, Long memberId) {
        Member member = memberLookupService.getOrThrowById(memberId);
        cookieUtils.clearAuthCookies(res);
        tokenStorage.removeRefreshToken(member);
    }
}
