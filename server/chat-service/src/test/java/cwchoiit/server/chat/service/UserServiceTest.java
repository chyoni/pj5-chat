package cwchoiit.server.chat.service;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.entity.User;
import cwchoiit.server.chat.repository.UserRepository;
import cwchoiit.server.chat.service.request.UserRegisterRequest;
import cwchoiit.server.chat.service.response.UserReadResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Service - UserService")
class UserServiceTest extends SpringBootTestConfiguration {

    @MockitoBean
    SessionService sessionService;

    @MockitoSpyBean
    UserRepository userRepository;

    @MockitoSpyBean
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    void definedRemoveUser() {
        User findUser = userRepository.findByUsername(sessionService.findUsername()).orElseThrow();
        userRepository.delete(findUser);
    }

    public Long createUser(UserRegisterRequest request) {
        User newUser = userRepository.save(
                User.create(
                        request.username(),
                        passwordEncoder.encode(request.password())
                )
        );
        return newUser.getUserId();
    }

    @Test
    @DisplayName("유저 생성이 정상적으로 수행된다.")
    void createUser() {
        Long userId = createUser(new UserRegisterRequest("test", "test"));

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(eq("test"));

        User test = userRepository.findByUsername("test").orElseThrow();
        assertThat(test).isNotNull();
        assertThat(test.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("이미 있는 유저이름과 동일한 이름으로 생성 시도하는 경우, 유저 생성에 실패한다.")
    void createUser_failed() {
        createUser(new UserRegisterRequest("test", "test"));
        User test = userRepository.findByUsername("test").orElseThrow();

        verify(passwordEncoder, times(1)).encode(eq("test"));
        assertThat(test).isNotNull();

        assertThatThrownBy(() -> createUser(new UserRegisterRequest("test", "any")))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("유저 삭제가 정상적으로 수행된다.")
    void removeUser() {
        userRepository.save(User.create("test", "test"));
        User saved = userRepository.findByUsername("test").orElseThrow();
        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("test");

        when(sessionService.findUsername()).thenReturn("test");

        definedRemoveUser();

        verify(sessionService, times(1)).findUsername();
        verify(userRepository, times(1)).delete(any(User.class));

        boolean isEmpty = userRepository.findByUsername("test").isEmpty();
        assertThat(isEmpty).isTrue();
    }

    @Test
    @DisplayName("로그인 하지 않은 상태에서는 유저 삭제가 불가능하다.")
    void removeUser_failed() {
        assertThatThrownBy(this::definedRemoveUser).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("없는 유저를 삭제하려는 경우, 유저 삭제가 불가능하다.")
    void removeUser_failed2() {
        when(sessionService.findUsername()).thenReturn("test");
        assertThatThrownBy(this::definedRemoveUser).isInstanceOf(Exception.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("유저 ID로 유저를 조회할 수 있다.")
    void findUsernameByUserId() {
        User save = userRepository.save(User.create("test", "test"));

        String username = userService.findUsernameByUserId(save.getUserId()).orElseThrow();

        assertThat(username).isEqualTo(save.getUsername());
        assertThat(username).isEqualTo("test");

        verify(userRepository, times(1)).findByUserId(eq(save.getUserId()));
    }

    @Test
    @DisplayName("없는 유저 ID로 유저를 조회하는 경우, 유저가 조회되지 않는다.")
    void findUsernameByUserId_failed() {
        boolean empty = userService.findUsernameByUserId(1L).isEmpty();
        assertThat(empty).isTrue();

        verify(userRepository, times(1)).findByUserId(eq(1L));
    }

    @Test
    @DisplayName("존재하는 유저이름으로 유저 ID를 찾으면, 유저가 조회된다.")
    void findUserIdByUsername() {
        User save = userRepository.save(User.create("test", "test"));

        Long userId = userService.findUserIdByUsername(save.getUsername()).orElseThrow();

        assertThat(userId).isNotNull();
        assertThat(userId).isEqualTo(save.getUserId());

        verify(userRepository, times(1)).findByUsername(eq(save.getUsername()));
    }

    @Test
    @DisplayName("존재하지 않는 유저이름으로 유저 ID를 찾으면, 유저가 조회되지 않는다.")
    void findUserIdByUsername_failed() {
        boolean empty = userService.findUserIdByUsername("Invalid").isEmpty();

        assertThat(empty).isTrue();

        verify(userRepository, times(1)).findByUsername(eq("Invalid"));
    }

    @Test
    @DisplayName("존재하는 유저 ID로 해당 유저의 초대 코드를 찾을 수 있다.")
    void findConnectionInviteCodeByUserId() {
        User newUser = User.create("test", "test");
        userRepository.save(newUser);

        String inviteCode = userService.findInviteCodeByUserId(newUser.getUserId()).orElseThrow();

        assertThat(inviteCode).isNotNull();
        assertThat(inviteCode).isEqualTo(newUser.getConnectionInviteCode());

        verify(userRepository, times(1)).findByUserId(eq(newUser.getUserId()));
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 초대 코드를 찾는 경우, 초대 코드는 반환되지 않는다.")
    void findConnectionInviteCodeByUserId_failed() {
        boolean empty = userService.findInviteCodeByUserId(1L).isEmpty();

        assertThat(empty).isTrue();

        verify(userRepository, times(1)).findByUserId(eq(1L));
    }

    @Test
    @DisplayName("유저 ID로 커넥션 횟수를 가져올 수 있다.")
    void findConnectionCountByUserId() {
        User save = userRepository.save(User.create("test", "test"));

        Integer connectionCount = userService.findConnectionCountByUserId(save.getUserId()).orElseThrow();

        assertThat(connectionCount).isNotNull();
        assertThat(connectionCount).isEqualTo(0);
    }

    @Test
    @DisplayName("없는 유저 ID로 커넥션 횟수를 가져오지 못한다.")
    void findConnectionCountByUserId_failed() {
        boolean empty = userService.findConnectionCountByUserId(1L).isEmpty();

        assertThat(empty).isTrue();

        verify(userRepository, times(1)).findByUserId(eq(1L));
    }

    @Test
    @DisplayName("초대 코드로 유저를 정상적으로 찾을 수 있다.")
    void findUserByConnectionInviteCode() {
        User save = userRepository.save(User.create("test", "test"));

        long noCachedStart = System.currentTimeMillis();
        UserReadResponse userReadResponse = userService.findUserByConnectionInviteCode(save.getConnectionInviteCode()).orElseThrow();
        long noCachedEnd = System.currentTimeMillis();

        assertThat(userReadResponse).isNotNull();
        assertThat(userReadResponse.userId()).isEqualTo(save.getUserId());
        assertThat(userReadResponse.username()).isEqualTo(save.getUsername());

        verify(userRepository, times(1)).findByConnectionInviteCode(eq(save.getConnectionInviteCode()));

        // 캐시 검증
        long cachedStart = System.currentTimeMillis();
        UserReadResponse cached = userService.findUserByConnectionInviteCode(save.getConnectionInviteCode()).orElseThrow();
        long cachedEnd = System.currentTimeMillis();

        assertThat(cached).isNotNull();
        assertThat(cached.userId()).isEqualTo(save.getUserId());
        assertThat(cached.username()).isEqualTo(save.getUsername());
        assertThat(cachedEnd - cachedStart).isLessThanOrEqualTo(noCachedEnd - noCachedStart);
    }

    @Test
    @DisplayName("없는 초대 코드로 유저를 찾을 경우, 유저를 찾지 못해야 한다.")
    void findUserByConnectionInviteCode_failed() {
        boolean empty = userService.findUserByConnectionInviteCode("any").isEmpty();

        assertThat(empty).isTrue();
        verify(userRepository, times(1)).findByConnectionInviteCode(eq("any"));
    }

    @Test
    @DisplayName("여러 유저이름으로 유저 ID를 복수로 찾을 수 있다.")
    void findUserIdByUsernameList() {
        User save1 = userRepository.save(User.create("test1", "test1"));
        User save2 = userRepository.save(User.create("test2", "test2"));
        User save3 = userRepository.save(User.create("test3", "test3"));

        List<Long> userIdsByUsernames = userService.findUserIdsByUsernames(List.of("test1", "test2", "test3"));

        assertThat(userIdsByUsernames).isNotNull();
        assertThat(userIdsByUsernames).containsExactlyInAnyOrder(save1.getUserId(), save2.getUserId(), save3.getUserId());
    }
}