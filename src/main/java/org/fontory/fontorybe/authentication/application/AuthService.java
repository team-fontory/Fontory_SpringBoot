package org.fontory.fontorybe.authentication.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 새롭게 토큰 발급
     * 기존에 토큰이 존재한다면 제거, 기존 토큰이 존재할 필요 X
     */
    public TokenResponse generateTokens(Member member) {
        UserPrincipal user = UserPrincipal.from(member);

        String existingToken = tokenService.getRefreshToken(member);
        if (existingToken != null) {
            tokenService.removeRefreshToken(member);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        tokenService.saveRefreshToken(member, refreshToken);

        return TokenResponse.from(accessToken, refreshToken);
    }
}
