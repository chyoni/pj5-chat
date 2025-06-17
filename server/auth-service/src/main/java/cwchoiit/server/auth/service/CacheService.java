package cwchoiit.server.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean delete(String key) {
        try {
            return stringRedisTemplate.delete(key);
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
}
