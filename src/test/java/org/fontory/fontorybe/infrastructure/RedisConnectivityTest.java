package org.fontory.fontorybe.infrastructure;

import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisConnectivityTest {

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

    @Test
    public void testProvideSerialization() {
        Provide provide = Provide.builder()
                .id(1L)
                .provider(Provider.GOOGLE)
                .providedId("114911176541439638085")
                .email("gndlapfhd@gmail.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .memberId(null)
                .build();

        RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();

        // 직렬화
        byte[] serializedData = serializer.serialize(provide);
        // 역직렬화
        Provide deserializedProvide = (Provide) serializer.deserialize(serializedData);

        // 원래 객체와 역직렬화한 객체 비교
        assertEquals(provide.getId(), deserializedProvide.getId());
        assertEquals(provide.getEmail(), deserializedProvide.getEmail());
        assertEquals(provide.getProvider(), deserializedProvide.getProvider());
    }
}
