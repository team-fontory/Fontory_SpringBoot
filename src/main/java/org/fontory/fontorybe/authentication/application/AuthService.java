package org.fontory.fontorybe.authentication.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse login(UserPrincipal user) {
        String existingToken = tokenService.getRefreshToken(user.getId());
        if (existingToken != null) {
            tokenService.removeRefreshToken(user.getId());
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        tokenService.saveRefreshToken(user.getId(), refreshToken);

        return TokenResponse.from(accessToken, refreshToken);
    }
}
