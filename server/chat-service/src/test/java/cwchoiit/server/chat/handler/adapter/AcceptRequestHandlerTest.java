package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.AcceptRequest;
import cwchoiit.server.chat.handler.request.InviteRequest;
import cwchoiit.server.chat.handler.request.KeepAliveRequest;
import cwchoiit.server.chat.handler.request.MessageRequest;
import cwchoiit.server.chat.handler.response.AcceptNotificationResponse;
import cwchoiit.server.chat.handler.response.AcceptResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.ACCEPT_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Handler Adapter - AcceptRequestHandler")
class AcceptRequestHandlerTest extends SpringBootTestConfiguration {

    @MockitoBean
    UserConnectionService userConnectionService;

    @MockitoBean
    ClientNotificationService clientNotificationService;

    @Autowired
    AcceptRequestHandler acceptRequestHandler;

    @Test
    @DisplayName("AcceptRequestHandler는 MessageType이 [ACCEPT_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = acceptRequestHandler.messageType();
        assertThat(messageType).isEqualTo(ACCEPT_REQUEST);
    }

    @Test
    @DisplayName("AcceptRequestHandler는 BaseRequest 인스턴스 타입이 AcceptRequest 일때 처리할 수 있다.")
    void handle() {
        acceptRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        acceptRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        acceptRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));

        verify(userConnectionService, never()).accept(anyLong(), anyString());
    }

    @Test
    @DisplayName("Accept 로직이 정상 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long acceptorId = 1L;
        long inviterId = 2L;

        attributes.put(IdKey.USER_ID.getValue(), acceptorId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.accept(eq(acceptorId), anyString()))
                .thenReturn(Pair.of(Optional.of(inviterId), "acceptor"));

        acceptRequestHandler.handle(new AcceptRequest("inviter"), mockSession);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(acceptorId), any(AcceptResponse.class));

        verify(clientNotificationService, times(1))
                .sendMessage(eq(inviterId), any(AcceptNotificationResponse.class));
    }

    @Test
    @DisplayName("Accept 실패 시, 실패 메시지를 전달한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long acceptorId = 1L;
        long inviterId = 2L;

        attributes.put(IdKey.USER_ID.getValue(), acceptorId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.accept(eq(acceptorId), anyString()))
                .thenReturn(Pair.of(Optional.empty(), "errorMessage"));

        acceptRequestHandler.handle(new AcceptRequest("inviter"), mockSession);

        verify(clientNotificationService, never())
                .sendMessage(eq(mockSession), eq(acceptorId), any(AcceptResponse.class));
        verify(clientNotificationService, never())
                .sendMessage(any(), eq(inviterId), any(AcceptNotificationResponse.class));
        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(acceptorId), any(ErrorResponse.class));
    }
}