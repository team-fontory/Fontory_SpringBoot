package org.fontory.fontorybe.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisConnectivityTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testRedisConnectivity() {
        final String key = "key";
        final String value = "value";

        redisTemplate.opsForValue().set(key, value);

        String storedValue = redisTemplate.opsForValue().get(key);

        assertThat(storedValue).isEqualTo(value);
    }
}
