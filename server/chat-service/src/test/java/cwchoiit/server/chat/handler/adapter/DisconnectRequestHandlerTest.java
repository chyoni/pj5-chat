package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.DisconnectResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static cwchoiit.server.chat.constants.MessageType.DISCONNECT_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - DisconnectRequestHandler")
class DisconnectRequestHandlerTest {

    @Mock
    UserConnectionService userConnectionService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    DisconnectRequestHandler disconnectRequestHandler;

    @Test
    @DisplayName("DisconnectRequestHandler MessageType이 [DISCONNECT_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = disconnectRequestHandler.messageType();
        assertThat(messageType).isEqualTo(DISCONNECT_REQUEST);
    }

    @Test
    @DisplayName("DisconnectRequestHandler는 BaseRequest 인스턴스 타입이 DisconnectRequest 일때 처리할 수 있다.")
    void handle() {
        disconnectRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        disconnectRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));

        verify(userConnectionService, never()).disconnect(anyLong(), anyString());
    }

    @Test
    @DisplayName("Disconnect 로직이 정상 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        String peer = "peer";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.disconnect(eq(requestUserId), eq(peer)))
                .thenReturn(Pair.of(true, peer));

        disconnectRequestHandler.handle(new DisconnectRequest(peer), mockSession);

        ArgumentCaptor<DisconnectResponse> captor = ArgumentCaptor.forClass(DisconnectResponse.class);
        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(requestUserId), captor.capture());
        DisconnectResponse value = captor.getValue();
        assertThat(value.getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);
        assertThat(value.getUsername()).isEqualTo(peer);

        verify(clientNotificationService, never())
                .sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("Disconnect 실패 시, 실패 메시지를 전달한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        String peer = "peer";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.disconnect(eq(requestUserId), eq(peer)))
                .thenReturn(Pair.of(false, "errorMessage"));

        disconnectRequestHandler.handle(new DisconnectRequest(peer), mockSession);

        ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(requestUserId), captor.capture());
        ErrorResponse value = captor.getValue();
        assertThat(value.getMessage()).isEqualTo("errorMessage");
        assertThat(value.getMessageType()).isEqualTo(DISCONNECT_REQUEST);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(requestUserId), any(DisconnectResponse.class));
    }
}