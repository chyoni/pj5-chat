package cwchoiit.chat.server.service;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.entity.UserConnection;
import cwchoiit.chat.server.repository.UserConnectionRepository;
import cwchoiit.chat.server.repository.UserRepository;
import cwchoiit.chat.server.repository.projection.UserIdWithName;
import cwchoiit.chat.server.service.response.UserReadResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("UserConnectionStatus, UserId를 통해 현재 연결된 유저 커넥션 정보를 가져올 수 있다.")
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

        assertThat(connections.size()).isEqualTo(3);
        assertThat(connections2.size()).isEqualTo(1);
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