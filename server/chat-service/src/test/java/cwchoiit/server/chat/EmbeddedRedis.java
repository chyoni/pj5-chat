package cwchoiit.server.chat;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 운영 환경의 Redis 와 테스트 환경의 Redis 분리하기 위해 Embedded Redis 사용
 */
@TestConfiguration
public class EmbeddedRedis {

    private static RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        if (redisServer == null || !redisServer.isActive()) {
            redisServer = new RedisServer(63790);
            redisServer.start();
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
