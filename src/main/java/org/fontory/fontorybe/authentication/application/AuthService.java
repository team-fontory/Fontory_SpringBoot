package org.fontory.fontorybe.authentication.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.application.dto.TokenResponse;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CookieUtils cookieUtils;
    private final TokenStorage tokenStorage;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    /**
     * 새롭게 토큰 발급
     * 기존에 토큰이 존재한다면 제거, 기존 토큰이 존재할 필요 X
     */
    public TokenResponse issueNewTokens(Member member) {
        UserPrincipal user = UserPrincipal.from(member);

        String existingToken = tokenStorage.getRefreshToken(member);
        if (existingToken != null) {
            tokenStorage.removeRefreshToken(member);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        tokenStorage.saveRefreshToken(member, refreshToken);

        return TokenResponse.from(accessToken, refreshToken);
    }

    public ResponseCookies issueAuthCookies(Member member) {
        TokenResponse tokenResponse = issueNewTokens(member);
        ResponseCookie accessTokenCookie = cookieUtils.createAccessTokenCookie(tokenResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = cookieUtils.createRefreshTokenCookie(tokenResponse.getRefreshToken());
        return ResponseCookies.from(accessTokenCookie, refreshTokenCookie);
    }

    public ResponseCookies refreshAuthCookies(Long memberId, String providedRefreshToken) {
        Member member = memberService.getOrThrowById(memberId);

        TokenResponse tokens = tokenStorage.refreshToken(member, providedRefreshToken);

        // 새 쿠키 생성
        ResponseCookie accessCookie  = cookieUtils.createAccessTokenCookie(tokens.getAccessToken());
        ResponseCookie refreshCookie = cookieUtils.createRefreshTokenCookie(tokens.getRefreshToken());

        return ResponseCookies.from(accessCookie, refreshCookie);
    }
}
