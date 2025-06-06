package cwchoiit.chat.server.service;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.entity.UserConnection;
import cwchoiit.chat.server.repository.UserConnectionRepository;
import cwchoiit.chat.server.repository.UserRepository;
import cwchoiit.chat.server.service.response.UserReadResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static cwchoiit.chat.server.constants.UserConnectionStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@Transactional
@SpringBootTest
@DisplayName("Service - UserConnectionService")
class UserConnectionServiceTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    UserService userService;

    @MockitoSpyBean
    UserConnectionRepository userConnectionRepository;

    @MockitoSpyBean
    UserRepository userRepository;

    @Autowired
    UserConnectionService userConnectionService;

    @Test
    @DisplayName("주어진 inviteCode로 찾은 유저가 없는 경우, 초대에 실패한다.")
    void invite_failed() {
        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "invalidCode");
        assertThat(invalid.getSecond()).isEqualTo("partner not found with invite code: invalidCode");
        assertThat(invalid.getFirst()).isEmpty();
    }

    @Test
    @DisplayName("주어진 inviteCode로 찾은 유저가 초대 요청을 한 유저랑 동일하면, 초대에 실패한다.")
    void invite_failed2() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(1L, "inviter")));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");
        assertThat(invalid.getSecond()).isEqualTo("Cannot invite self.");
        assertThat(invalid.getFirst()).isEmpty();
    }

    @Test
    @DisplayName("주어진 inviteCode로 이미 초대 요청이 수락된 상태(ACCEPTED)라면, 초대에 실패한다.")
    void invite_failed3() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, ACCEPTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("Already invited.");
        assertThat(invalid.getFirst()).isEmpty();

        verify(userService, never()).findUsernameByUserId(anyLong());
    }

    @Test
    @DisplayName("주어진 inviteCode로 이미 초대 요청이 들어간 상태(PENDING)라면, 초대에 실패한다.")
    void invite_failed4() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, PENDING)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("Already invited or rejected.");
        assertThat(invalid.getFirst()).isEmpty();

        verify(userService, never()).findUsernameByUserId(anyLong());
    }

    @Test
    @DisplayName("주어진 inviteCode로 이미 초대 요청이 거부된 상태(REJECTED)라면, 초대에 실패한다.")
    void invite_failed5() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, REJECTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("Already invited or rejected.");
        assertThat(invalid.getFirst()).isEmpty();

        verify(userService, never()).findUsernameByUserId(anyLong());
    }

    @Test
    @DisplayName("주어진 inviteCode로 초대를 한 초대 상태가 NONE 이고, 초대자가 없는 유저인 경우 초대에 실패한다.")
    void invite_failed6() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.empty());

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, NONE)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter not found: 1");
        assertThat(invalid.getFirst()).isEmpty();
    }

    @Test
    @DisplayName("주어진 inviteCode로 초대를 한 초대 상태가 DISCONNECTED 이고, 초대자가 없는 유저인 경우 초대에 실패한다.")
    void invite_failed7() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.empty());

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, DISCONNECTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter not found: 1");
        assertThat(invalid.getFirst()).isEmpty();
    }

    @Test
    @DisplayName("주어진 inviteCode로 초대 요청에 대한 상태가 최초(NONE)이지만, 초대자의 초대 제한 횟수가 최대값을 초과한 경우 초대에 실패한다.")
    void invite_failed8() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, NONE)));

        when(userService.findConnectionCountByUserId(1L)).thenReturn(Optional.of(5000000));

        Pair<Optional<Long>, String> result = userConnectionService.invite(1L, "code");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Connection count limit exceeded.");
    }

    @Test
    @DisplayName("주어진 inviteCode로 초대 요청에 대한 상태가 최초(DISCONNECTED)이지만, 초대자의 초대 제한 횟수가 최대값을 초과한 경우 초대에 실패한다.")
    void invite_failed9() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, DISCONNECTED)));

        when(userService.findConnectionCountByUserId(1L)).thenReturn(Optional.of(5000000));

        Pair<Optional<Long>, String> result = userConnectionService.invite(1L, "code");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Connection count limit exceeded.");
    }

    // 아래에서 Mock 객체로 테스트하고 있기 때문에, 실제 데이터베이스에 저장되는 것을 확인할 수 없음.
    // 여러가지 방법이 있겠지만, 이렇게 Captor를 사용해서 실제 가짜 Mock 객체인 userConnectionRepository이 save()가 실행될때 반환되는 객체를 캡쳐해서
    // 검증할 수 있음
    @Captor
    ArgumentCaptor<UserConnection> connectionArgumentCaptor;

    @Test
    @DisplayName("주어진 inviteCode로 초대 요청에 대한 상태가 최초(NONE)인 경우, 초대에 성공한다.")
    void invite_success() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter");
        assertThat(invalid.getFirst()).isEqualTo(Optional.of(2L));

        verify(userService, times(1)).findUsernameByUserId(eq(1L));

        verify(userConnectionRepository).save(connectionArgumentCaptor.capture());
        UserConnection captorValue = connectionArgumentCaptor.getValue();
        assertThat(captorValue.getStatus()).isEqualTo(PENDING);
        assertThat(captorValue.getInviterUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주어진 inviteCode로 초대 요청에 대한 상태가 미연결(DISCONNECTED)인 경우, 초대에 성공한다.")
    void invite_success2() {
        when(userService.findUserByConnectionInviteCode(eq("code")))
                .thenReturn(Optional.of(new UserReadResponse(2L, "partner")));

        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("inviter"));

        when(userConnectionRepository.findUserConnectionBy(anyLong(), anyLong()))
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, DISCONNECTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter");
        assertThat(invalid.getFirst()).isEqualTo(Optional.of(2L));

        verify(userService, times(1)).findUsernameByUserId(eq(1L));

        verify(userConnectionRepository).save(connectionArgumentCaptor.capture());
        UserConnection captorValue = connectionArgumentCaptor.getValue();
        assertThat(captorValue.getStatus()).isEqualTo(PENDING);
        assertThat(captorValue.getInviterUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("초대 수락 - 초대한 사람의 유저이름이 잘못된 경우 예외가 발생한다.")
    void accept_failed() {
        assertThatThrownBy(() -> userConnectionService.accept(1L, "Invalid"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 수락 - 초대한 사람과 초대받는 사람이 동일한 경우, 예외가 발생한다.")
    void accept_failed2() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));

        Pair<Optional<Long>, String> result = userConnectionService.accept(1L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Cannot accept self.");
    }

    @Test
    @DisplayName("초대 수락 - 데이터베이스에서 유저 커넥션 레코드를 가져왔을 때 실제 초대자와, 초대한 사람의 이름으로 유저를 찾았을 때 유저 정보가 다르다면 예외가 발생한다.")
    void accept_failed3() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 2L, PENDING)));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Invalid inviter's connection.");
    }

    @Test
    @DisplayName("초대 수락 - 초대 상태가 이미 ACCEPTED인 경우, 예외가 발생한다.")
    void accept_failed4() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Already accepted.");
    }

    @Test
    @DisplayName("초대 수락 - 초대 상태가 REJECTED 라면, 예외가 발생한다.")
    void accept_failed5() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, REJECTED)));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Invalid status: " + REJECTED);
    }

    @Test
    @DisplayName("초대 수락 - 초대 상태가 NONE 이라면, 예외가 발생한다.")
    void accept_failed6() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, NONE)));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Invalid status: " + NONE);
    }

    @Test
    @DisplayName("초대 수락 - 초대 상태가 DISCONNECTED 이라면, 예외가 발생한다.")
    void accept_failed7() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, DISCONNECTED)));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo("Invalid status: " + DISCONNECTED);
    }

    @Test
    @DisplayName("초대 수락 - 초대 수락자가 없는 경우, 예외가 발생한다.")
    void accept_failed8() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        when(userService.findUsernameByUserId(eq(2L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 수락 - 파트너 A 유저를 비관적 락을 통해 가져올 때 예외가 발생한 경우, 초대 수락에 실패한다.")
    void accept_failed9() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 수락 - 파트너 B 유저를 비관적 락을 통해 가져올 때 예외가 발생한 경우, 초대 수락에 실패한다.")
    void accept_failed10() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 수락 - 파트너 A, 파트너 B, 초대 상태 PENDING인 레코드를 찾지 못한 경우, 예외가 발생한다.")
    void accept_failed11() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");
        partnerA.changeConnectionCount(50000);

        when(userRepository.findLockByUserId(1L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(PENDING.name())))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 수락 - 파트너 A의 초대 수 제한이 초과한 경우, 초대 수락에 실패한다.")
    void accept_failed12() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");
        partnerA.changeConnectionCount(50000);

        when(userRepository.findLockByUserId(1L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(PENDING.name())))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection count limit exceeded");
    }

    @Test
    @DisplayName("초대 수락 - 파트너 B의 초대 수 제한이 초과한 경우, 초대 수락에 실패한다.")
    void accept_failed13() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");
        partnerB.changeConnectionCount(50000);

        when(userRepository.findLockByUserId(1L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(PENDING.name())))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection count limit exceeded");
    }

    @Test
    @DisplayName("초대 수락 - inviter가 파트너 B이고, 파트너 B의 초대 수 제한이 초과한 경우, 초대 수락에 실패한다.")
    void accept_failed_added() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(100L));
        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(100L)))
                .thenReturn(Optional.of(UserConnection.create(2L, 100L, 100L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");
        partnerB.changeConnectionCount(50000);

        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(100L)).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(100L), eq(PENDING.name())))
                .thenReturn(Optional.of(UserConnection.create(2L, 100L, 100L, PENDING)));

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageMatching("Connection count limit exceeded.");
    }

    @Test
    @DisplayName("초대 수락 - inviter가 파트너 B이고, 파트너 A의 초대 수 제한이 초과한 경우, 초대 수락에 실패한다.")
    void accept_failed_added2() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(100L));
        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(100L)))
                .thenReturn(Optional.of(UserConnection.create(2L, 100L, 100L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");
        partnerA.changeConnectionCount(50000);

        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(100L)).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(100L), eq(PENDING.name())))
                .thenReturn(Optional.of(UserConnection.create(2L, 100L, 100L, PENDING)));

        assertThatThrownBy(() -> userConnectionService.accept(2L, "inviter"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection count limit exceeded by partner.");
    }

    @Test
    @DisplayName("초대 수락 - 모든 검증에 통과한 경우, 초대 수락에 성공한다.")
    void accept_success() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));
        when(userService.findUsernameByUserId(eq(2L))).thenReturn(Optional.of("acceptor"));

        User partnerA = User.create("inviter", "inviter");
        User partnerB = User.create("acceptor", "acceptor");

        when(userRepository.findLockByUserId(1L)).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(2L)).thenReturn(Optional.of(partnerB));

        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, PENDING);
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(PENDING.name())))
                .thenReturn(Optional.of(userConnection));

        Pair<Optional<Long>, String> result = userConnectionService.accept(2L, "inviter");

        assertThat(result.getFirst()).isEqualTo(Optional.of(1L));
        assertThat(result.getSecond()).isEqualTo("acceptor");

        assertThat(partnerA.getConnectionCount()).isEqualTo(1);
        assertThat(partnerB.getConnectionCount()).isEqualTo(1);
        assertThat(userConnection.getStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    @DisplayName("초대 거절 - 초대자의 이름으로 초대자를 찾았을 때, 찾지 못한 경우, 예외를 반환한다.")
    void reject_failed() {
        assertThatThrownBy(() -> userConnectionService.reject(1L, "Invalid"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("초대 거절 - 초대자와 거절하는 사람이 동일한 ID인 경우, 초대 거절에 실패한다.")
    void reject_failed2() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));

        Pair<Boolean, String> result = userConnectionService.reject(1L, "inviter");

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Invalid inviter's connection.");
    }

    @Test
    @DisplayName("초대 거절 - 초대 거절 시, 초대자의 이름으로 찾은 초대자의 ID와 두 유저간 커넥션의 실 초대자가 다른 경우, 초대 거절에 실패한다.")
    void reject_failed3() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 2L, PENDING)));
        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(1L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 2L, PENDING)));

        Pair<Boolean, String> result = userConnectionService.reject(2L, "inviter");

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Invalid inviter's connection.");
    }

    @Test
    @DisplayName("초대 거절 - 초대 거절 시, 초대 상태가 PENDING이 아니라면 초대 거절에 실패한다.")
    void reject_failed4() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        Pair<Boolean, String> result = userConnectionService.reject(2L, "inviter");

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Invalid status: " + ACCEPTED);
    }

    @Test
    @DisplayName("초대 거절 - 초대 거절에 성공한다.")
    void reject_success() {
        when(userService.findUserIdByUsername("inviter")).thenReturn(Optional.of(1L));

        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, PENDING);
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(userConnection));

        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(1L)))
                .thenReturn(Optional.of(userConnection));

        Pair<Boolean, String> result = userConnectionService.reject(2L, "inviter");

        assertThat(result.getFirst()).isTrue();
        assertThat(result.getSecond()).isEqualTo("inviter");

        assertThat(userConnection.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    @DisplayName("연결 해제 - 연결 해제 대상 유저 이름으로 유저를 찾지 못하면 연결 해제에 실패한다.")
    void disconnect_failed() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.empty());

        Pair<Boolean, String> result = userConnectionService.disconnect(1L, peerUsername);

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Peer not found.");
    }

    @Test
    @DisplayName("연결 해제 - 연결 해제 대상 유저 이름으로 유저를 찾았을 때, 해당 유저가 연결 해제 요청한 유저와 동일한 경우 연결 해제에 실패한다.")
    void disconnect_failed2() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        Pair<Boolean, String> result = userConnectionService.disconnect(1L, peerUsername);

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Peer not found.");
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED, REJECTED가 아닌 경우엔, 연결 해제에 실패한다.")
    void disconnect_failed3() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, PENDING)));

        Pair<Boolean, String> result = userConnectionService.disconnect(2L, peerUsername);

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Disconnect failed.");
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED인 경우, 연결 해제를 시도하지만, ID를 통해 파트너 A를 찾지 못한 경우, 예외가 발생한다.")
    void disconnect_failed4() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.disconnect(2L, peerUsername))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED인 경우, 연결 해제를 시도하지만, ID를 통해 파트너 B를 찾지 못한 경우, 예외가 발생한다.")
    void disconnect_failed5() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.disconnect(2L, peerUsername))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED인 경우, 연결 해제를 시도하지만, 데이터베이스에서 두 유저의 연결 상태가 ACCEPTED인 연결을 찾지못하면, 예외가 발생한다.")
    void disconnect_failed6() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.of(User.create("peer", "peer")));
        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.of(User.create("caller", "caller")));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(ACCEPTED.name())))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userConnectionService.disconnect(2L, peerUsername))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED인 경우, 연결 해제를 시도하지만, 파트너 A의 현재 연결 수가 0이라면 예외가 발생한다.")
    void disconnect_failed7() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        User partnerA = User.create("peer", "peer");
        User partnerB = User.create("caller", "caller");

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(ACCEPTED.name())))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        assertThatThrownBy(() -> userConnectionService.disconnect(2L, peerUsername))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("peer: connection count is already 0.");
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 ACCEPTED인 경우, 연결 해제를 시도하지만, 파트너 B의 현재 연결 수가 0이라면 예외가 발생한다.")
    void disconnect_failed8() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        User partnerA = User.create("peer", "peer");
        partnerA.changeConnectionCount(1);
        User partnerB = User.create("caller", "caller");

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.of(partnerB));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(ACCEPTED.name())))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        assertThatThrownBy(() -> userConnectionService.disconnect(2L, peerUsername))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("caller: connection count is already 0.");
    }

    @Test
    @DisplayName("연결 해제 - 연결 해제에 성공한다.")
    void disconnect_success() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, ACCEPTED)));

        User partnerA = User.create("peer", "peer");
        partnerA.changeConnectionCount(1);
        User partnerB = User.create("caller", "caller");
        partnerB.changeConnectionCount(1);

        when(userRepository.findLockByUserId(eq(1L))).thenReturn(Optional.of(partnerA));
        when(userRepository.findLockByUserId(eq(2L))).thenReturn(Optional.of(partnerB));

        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, ACCEPTED);
        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L), eq(ACCEPTED.name())))
                .thenReturn(Optional.of(userConnection));

        Pair<Boolean, String> result = userConnectionService.disconnect(2L, peerUsername);

        assertThat(result.getFirst()).isTrue();
        assertThat(result.getSecond()).isEqualTo("peer");

        assertThat(partnerA.getConnectionCount()).isEqualTo(0);
        assertThat(partnerB.getConnectionCount()).isEqualTo(0);
        assertThat(userConnection.getStatus()).isEqualTo(DISCONNECTED);
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 REJECTED 이고, 초대자가 연결 해제를 요청한 사람라면 연결 해제에 실패한다.")
    void disconnect_failed_rejected() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 2L, REJECTED)));

        UserConnection userConnection = UserConnection.create(1L, 2L, 2L, REJECTED);
        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(1L)))
                .thenReturn(Optional.of(userConnection));

        Pair<Boolean, String> result = userConnectionService.disconnect(2L, peerUsername);

        assertThat(result.getFirst()).isFalse();
        assertThat(result.getSecond()).isEqualTo("Disconnect failed.");

        assertThat(userConnection.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    @DisplayName("연결 해제 - 연결 상태가 REJECTED 이고, 초대자가 연결 해제를 요청한 사람이 아니라면 연결 해제에 성공한다.")
    void disconnect_success2() {
        String peerUsername = "peer";
        when(userService.findUserIdByUsername(peerUsername)).thenReturn(Optional.of(1L));

        when(userConnectionRepository.findUserConnectionBy(eq(1L), eq(2L)))
                .thenReturn(Optional.of(UserConnection.create(1L, 2L, 1L, REJECTED)));

        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, REJECTED);
        when(userConnectionRepository.findUserConnectionBy(eq(2L), eq(1L)))
                .thenReturn(Optional.of(userConnection));

        Pair<Boolean, String> result = userConnectionService.disconnect(2L, peerUsername);

        assertThat(result.getFirst()).isTrue();
        assertThat(result.getSecond()).isEqualTo("peer");

        assertThat(userConnection.getStatus()).isEqualTo(DISCONNECTED);
    }
}