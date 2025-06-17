package cwchoiit.server.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import static cwchoiit.server.chat.constants.KeyPrefix.USER_SESSION;

@Configuration
@EnableRedisHttpSession(
        redisNamespace = USER_SESSION,
        maxInactiveIntervalInSeconds = 600,
        flushMode = FlushMode.IMMEDIATE // 속성을 변경하면 바로 변경내용 플러쉬, 웹 소켓 요청이 들어와서 웹 소켓의 세션 유지 요청을 받으면 요청 처리 후 즉각 세션 만료 시간을 늘려주기 위해
)
public class RedisSessionConfig {

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
