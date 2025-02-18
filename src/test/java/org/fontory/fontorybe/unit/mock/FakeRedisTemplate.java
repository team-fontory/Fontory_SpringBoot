package org.fontory.fontorybe.unit.mock;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * FakeRedisTemplate: 실제 Redis 대신 in-memory Map을 사용하는 RedisTemplate Fake 구현체.
 */
public class FakeRedisTemplate extends RedisTemplate<String, String> {

    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final FakeValueOperations fakeValueOperations = new FakeValueOperations(store);

    @Override
    public ValueOperations<String, String> opsForValue() {
        return fakeValueOperations;
    }

}

class FakeValueOperations implements ValueOperations<String, String> {

    private final Map<String, String> store;

    public FakeValueOperations(Map<String, String> store) {
        this.store = store;
    }

    @Override
    public void set(String key, String value) {
        store.put(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        set(key, value, Duration.ofMillis(unit.toMillis(timeout)));
    }

    public void set(String key, String value, Duration timeout) {
        store.put(key, value);
    }

    @Override
    public Boolean setIfAbsent(String key, String value) {
        return null;
    }

    @Override
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public Boolean setIfPresent(String key, String value) {
        return null;
    }

    @Override
    public Boolean setIfPresent(String key, String value, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public void multiSet(Map<? extends String, ? extends String> map) {

    }

    @Override
    public Boolean multiSetIfAbsent(Map<? extends String, ? extends String> map) {
        return null;
    }

    @Override
    public String get(Object key) {
        return store.get(key.toString());
    }

    @Override
    public String getAndDelete(String key) {
        return "";
    }

    @Override
    public String getAndExpire(String key, long timeout, TimeUnit unit) {
        return "";
    }

    @Override
    public String getAndExpire(String key, Duration timeout) {
        return "";
    }

    @Override
    public String getAndPersist(String key) {
        return "";
    }

    @Override
    public String getAndSet(String key, String value) {
        return "";
    }

    @Override
    public List<String> multiGet(Collection<String> keys) {
        return List.of();
    }

    @Override
    public Long increment(String key) {
        return 0L;
    }

    @Override
    public Long increment(String key, long delta) {
        return 0L;
    }

    @Override
    public Double increment(String key, double delta) {
        return 0.0;
    }

    @Override
    public Long decrement(String key) {
        return 0L;
    }

    @Override
    public Long decrement(String key, long delta) {
        return 0L;
    }

    @Override
    public Integer append(String key, String value) {
        return 0;
    }

    @Override
    public String get(String key, long start, long end) {
        return "";
    }

    @Override
    public void set(String key, String value, long offset) {

    }

    @Override
    public Long size(String key) {
        return 0L;
    }

    @Override
    public Boolean setBit(String key, long offset, boolean value) {
        return null;
    }

    @Override
    public Boolean getBit(String key, long offset) {
        return null;
    }

    @Override
    public List<Long> bitField(String key, BitFieldSubCommands subCommands) {
        return List.of();
    }

    @Override
    public RedisOperations<String, String> getOperations() {
        return null;
    }
}