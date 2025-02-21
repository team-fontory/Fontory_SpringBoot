package org.fontory.fontorybe.authentication.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    public void saveRefreshToken(Long id, String refreshToken) {
        redisTemplate.opsForValue().set(KEY_PREFIX + id, refreshToken, Duration.ofMillis(REFRESH_TOKEN_VALIDITY));
    }

    public String getRefreshToken(Long id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }

    public void removeRefreshToken(Long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

    public TokenResponse refreshToken(String refreshToken) {
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        String storedRefreshToken = getRefreshToken(memberId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        UserPrincipal userPrincipal = new UserPrincipal(memberId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        saveRefreshToken(memberId, newRefreshToken);

        return TokenResponse.from(newAccessToken, newRefreshToken);
    }
}
