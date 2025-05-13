package org.fontory.fontorybe.sms.adapter.outbound;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.sms.application.port.PhoneNumberStorage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPhoneNumberStorage implements PhoneNumberStorage {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "phone_number:";
    private static final Duration TTL = Duration.ofHours(24);

    @Override
    public void savePhoneNumber(Font font, String phoneNumber) {
        redisTemplate.opsForValue().set(KEY_PREFIX + font.getId(), phoneNumber, TTL);
    }

    @Override
    public void removePhoneNumber(Font font) {
        redisTemplate.delete(KEY_PREFIX + font.getId());
    }

    @Override
    public String getPhoneNumber(Font font) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + font.getId());
    }
}
