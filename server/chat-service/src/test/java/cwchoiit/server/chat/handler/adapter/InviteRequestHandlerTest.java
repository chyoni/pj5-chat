package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.handler.request.AcceptRequest;
import cwchoiit.server.chat.handler.request.InviteRequest;
import cwchoiit.server.chat.handler.request.KeepAliveRequest;
import cwchoiit.server.chat.handler.request.MessageRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.InviteNotificationResponse;
import cwchoiit.server.chat.handler.response.InviteResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.INVITE_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Handler Adapter - InviteRequestHandler")
class InviteRequestHandlerTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    UserConnectionService userConnectionService;

    @MockitoSpyBean
    ClientNotificationService clientNotificationService;

    @Autowired
    InviteRequestHandler inviteRequestHandler;

    @Test
    @DisplayName("InviteRequestHandler는 MessageType이 [INVITE_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = inviteRequestHandler.messageType();
        assertThat(messageType).isEqualTo(INVITE_REQUEST);
    }

    @Test
    @DisplayName("InviteRequestHandler는 BaseRequest 인스턴스 타입이 InviteRequest일 때 처리할 수 있다.")
    void handle() {
        inviteRequestHandler.handle(new MessageRequest(1L, "test"), mock(WebSocketSession.class));
        inviteRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        inviteRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));

        verify(userConnectionService, never()).invite(anyLong(), anyString());
    }

    @Test
    @DisplayName("초대 로직이 정상적으로 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);

        long inviterUserId = 1L;
        String inviteCode = "test";

        InviteRequest inviteRequest = new InviteRequest(inviteCode);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), inviterUserId);

        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.invite(eq(inviterUserId), eq(inviteRequest.getConnectionInviteCode())))
                .thenReturn(Pair.of(Optional.of(2L), ""));

        inviteRequestHandler.handle(inviteRequest, mockSession);

        ArgumentCaptor<InviteResponse> captor = ArgumentCaptor.forClass(InviteResponse.class);
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(1L), captor.capture());

        InviteResponse value = captor.getValue();
        assertThat(value.getConnectionInviteCode()).isEqualTo(inviteCode);
        assertThat(value.getStatus()).isEqualTo(UserConnectionStatus.PENDING);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(2L), any(InviteNotificationResponse.class));

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(1L), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("초대 과정 중 문제가 발생하면, 응답 메시지로 에러 메시지를 보내준다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);

        long inviterUserId = 1L;
        String inviteCode = "test";

        InviteRequest inviteRequest = new InviteRequest(inviteCode);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), inviterUserId);

        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.invite(eq(inviterUserId), eq(inviteRequest.getConnectionInviteCode())))
                .thenReturn(Pair.of(Optional.empty(), "error"));

        inviteRequestHandler.handle(inviteRequest, mockSession);

        ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(1L), any(InviteResponse.class));

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(1L), captor.capture());

        ErrorResponse value = captor.getValue();
        assertThat(value.getMessageType()).isEqualTo(INVITE_REQUEST);
        assertThat(value.getMessage()).isEqualTo("error");
    }
}