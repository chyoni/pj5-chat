package cwchoiit.server.chat.service;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.entity.User;
import cwchoiit.server.chat.entity.UserConnection;
import cwchoiit.server.chat.repository.UserConnectionRepository;
import cwchoiit.server.chat.repository.UserRepository;
import cwchoiit.server.chat.service.response.UserReadResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional
@SpringBootTest
@DisplayName("Service - UserConnectionService for Query")
class UserConnectionServiceQueryTest extends SpringBootTestConfiguration {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserConnectionRepository userConnectionRepository;
    @Autowired
    UserConnectionService userConnectionService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("UserConnectionStatus, UserId를 통해 현재 연결된 유저 커넥션 정보를 가져올 수 있다. (ACCEPTED 상태가 아닌 경우)")
    void findConnectionUsersByStatus() {
        User u1 = userRepository.save(User.create("u1", "u1"));
        User u2 = userRepository.save(User.create("u2", "u2"));
        User u3 = userRepository.save(User.create("u3", "u3"));
        User u4 = userRepository.save(User.create("u4", "u4"));

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u2.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u3.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u4.getUserId(),
                        u1.getUserId(),
                        u4.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        List<UserReadResponse> connections = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.PENDING
        );
        List<UserReadResponse> connections2 = userConnectionService.findConnectionUsersByStatus(
                u2.getUserId(),
                UserConnectionStatus.PENDING
        );

        assertThat(connections.size()).isEqualTo(1);
        assertThat(connections2.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("UserConnectionStatus, UserId를 통해 현재 연결된 유저 커넥션 정보를 가져올 수 있다. (ACCEPTED 상태인 경우)")
    void findConnectionUsersByStatus2() {
        User u1 = userRepository.save(User.create("u1", "u1"));
        User u2 = userRepository.save(User.create("u2", "u2"));
        User u3 = userRepository.save(User.create("u3", "u3"));
        User u4 = userRepository.save(User.create("u4", "u4"));

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u2.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u3.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u4.getUserId(),
                        u1.getUserId(),
                        u4.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        List<UserReadResponse> connections = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.ACCEPTED
        );
        List<UserReadResponse> connections2 = userConnectionService.findConnectionUsersByStatus(
                u2.getUserId(),
                UserConnectionStatus.ACCEPTED
        );

        assertThat(connections.size()).isEqualTo(3);
        assertThat(connections2.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("캐시가 저장된 경우, UserConnectionStatus, UserId를 통해 현재 연결된 유저 커넥션 정보를 캐시로부터 가져올 수 있다.")
    void findConnectionUsersByStatus3() {
        User u1 = userRepository.save(User.create("u1", "u1"));
        User u2 = userRepository.save(User.create("u2", "u2"));
        User u3 = userRepository.save(User.create("u3", "u3"));
        User u4 = userRepository.save(User.create("u4", "u4"));

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u2.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u3.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u4.getUserId(),
                        u1.getUserId(),
                        u4.getUserId(),
                        UserConnectionStatus.ACCEPTED)
        );

        long noCachedStart = System.currentTimeMillis();
        List<UserReadResponse> connections = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.ACCEPTED
        );
        long noCachedEnd = System.currentTimeMillis();
        List<UserReadResponse> connections2 = userConnectionService.findConnectionUsersByStatus(
                u2.getUserId(),
                UserConnectionStatus.ACCEPTED
        );

        assertThat(connections.size()).isEqualTo(3);
        assertThat(connections2.size()).isEqualTo(1);

        // 캐시 검증
        long cachedStart = System.currentTimeMillis();
        List<UserReadResponse> cached = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.ACCEPTED
        );
        long cachedEnd = System.currentTimeMillis();

        assertThat(cached.size()).isEqualTo(3);
        assertThat(cachedEnd - cachedStart).isLessThanOrEqualTo(noCachedEnd - noCachedStart);
    }

    @Test
    @DisplayName("잘못된 UserConnectionStatus, UserId를 통해서 가져올 수 없는 경우 체크")
    void findConnectionUsersByStatus_invalid() {
        User u1 = userRepository.save(User.create("u1", "u1"));
        User u2 = userRepository.save(User.create("u2", "u2"));
        User u3 = userRepository.save(User.create("u3", "u3"));
        User u4 = userRepository.save(User.create("u4", "u4"));

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u2.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u1.getUserId(),
                        u3.getUserId(),
                        u1.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        userConnectionRepository.save(
                UserConnection.create(
                        u4.getUserId(),
                        u1.getUserId(),
                        u4.getUserId(),
                        UserConnectionStatus.PENDING)
        );

        List<UserReadResponse> invalidConn = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.NONE
        );
        List<UserReadResponse> invalidConn2 = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.DISCONNECTED
        );
        List<UserReadResponse> invalidConn3 = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.ACCEPTED
        );
        List<UserReadResponse> invalidConn4 = userConnectionService.findConnectionUsersByStatus(
                u1.getUserId(),
                UserConnectionStatus.REJECTED
        );

        assertThat(invalidConn.size()).isEqualTo(0);
        assertThat(invalidConn2.size()).isEqualTo(0);
        assertThat(invalidConn3.size()).isEqualTo(0);
        assertThat(invalidConn4.size()).isEqualTo(0);
    }
}