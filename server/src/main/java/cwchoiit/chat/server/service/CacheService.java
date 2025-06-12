package cwchoiit.chat.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public Optional<String> get(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public List<String> get(Collection<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    public void set(String key, String value, Long ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    public void expire(String key, Long ttl) {
        stringRedisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    public boolean delete(String key) {
        try {
            stringRedisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            log.error("[delete] Redis delete failed. key = {}", key, e);
            return false;
        }
    }

    public boolean delete(Collection<String> keys) {
        try {
            stringRedisTemplate.delete(keys);
            return true;
        } catch (Exception e) {
            log.error("[delete] Redis delete failed. keys = {}", keys, e);
            return false;
        }
    }

    public String generateKey(String prefix, String key) {
        return "%s:%s".formatted(prefix, key);
    }

    public String generateKey(String prefix, String key1, String key2) {
        return "%s:%s:%s".formatted(prefix, key1, key2);
    }
}
