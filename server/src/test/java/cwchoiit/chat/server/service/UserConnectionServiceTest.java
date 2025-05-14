package cwchoiit.chat.server.service;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.UserConnection;
import cwchoiit.chat.server.repository.UserConnectionRepository;
import cwchoiit.chat.server.service.response.UserReadResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Service - UserConnectionService")
class UserConnectionServiceTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    UserService userService;

    @MockitoSpyBean
    UserConnectionRepository userConnectionRepository;

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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.ACCEPTED)));

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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.PENDING)));

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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.REJECTED)));

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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.NONE)));

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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.DISCONNECTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter not found: 1");
        assertThat(invalid.getFirst()).isEmpty();
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
        assertThat(captorValue.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
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
                .thenReturn(Optional.of(UserConnection.create(2L, 1L, 1L, UserConnectionStatus.DISCONNECTED)));

        Pair<Optional<Long>, String> invalid = userConnectionService.invite(1L, "code");

        assertThat(invalid.getSecond()).isEqualTo("inviter");
        assertThat(invalid.getFirst()).isEqualTo(Optional.of(2L));

        verify(userService, times(1)).findUsernameByUserId(eq(1L));

        verify(userConnectionRepository).save(connectionArgumentCaptor.capture());
        UserConnection captorValue = connectionArgumentCaptor.getValue();
        assertThat(captorValue.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
        assertThat(captorValue.getInviterUserId()).isEqualTo(1L);
    }
}