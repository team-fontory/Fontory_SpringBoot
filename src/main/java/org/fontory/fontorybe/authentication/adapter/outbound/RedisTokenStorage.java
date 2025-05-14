package org.fontory.fontorybe.authentication.adapter.outbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenStorage implements TokenStorage {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private static final String KEY_PREFIX = "refresh_token:";

    public void saveRefreshToken(Member member, String refreshToken) {
        redisTemplate.opsForValue().set(KEY_PREFIX + member.getId(), refreshToken, Duration.ofMillis(jwtProperties.getRefreshTokenValidityMs()));
    }

    public String getRefreshToken(Member member) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + member.getId());
    }

    public void removeRefreshToken(Member member) {
        redisTemplate.delete(KEY_PREFIX + member.getId());
    }
}
