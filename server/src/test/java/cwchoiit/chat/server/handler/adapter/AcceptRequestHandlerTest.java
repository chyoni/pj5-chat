package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.Constants;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.InviteRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.handler.response.AcceptNotificationResponse;
import cwchoiit.chat.server.handler.response.AcceptResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.UserConnectionService;
import cwchoiit.chat.server.session.WebSocketSessionManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Handler Adapter - AcceptRequestHandler")
class AcceptRequestHandlerTest extends SpringBootTestConfiguration {

    @MockitoBean
    UserConnectionService userConnectionService;

    @MockitoBean
    WebSocketSessionManager sessionManager;

    @Autowired
    AcceptRequestHandler acceptRequestHandler;

    @Test
    @DisplayName("AcceptRequestHandler는 MessageType이 [ACCEPT_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = acceptRequestHandler.messageType();
        assertThat(messageType).isEqualTo("ACCEPT_REQUEST");
    }

    @Test
    @DisplayName("AcceptRequestHandler는 BaseRequest 인스턴스 타입이 AcceptRequest일때 처리할 수 있다.")
    void handle() {
        acceptRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        acceptRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        acceptRequestHandler.handle(new MessageRequest("123", "123"), mock(WebSocketSession.class));

        verify(userConnectionService, never()).accept(anyLong(), anyString());
    }

    @Test
    @DisplayName("Accept 로직이 정상 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long acceptorId = 1L;
        long inviterId = 2L;

        attributes.put(Constants.USER_ID.getValue(), acceptorId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.accept(eq(acceptorId), anyString()))
                .thenReturn(Pair.of(Optional.of(inviterId), "acceptor"));

        acceptRequestHandler.handle(new AcceptRequest("inviter"), mockSession);

        verify(sessionManager, times(1))
                .sendMessage(eq(mockSession), any(AcceptResponse.class));

        verify(sessionManager, times(1)).findSessionByUserId(eq(inviterId));

        verify(sessionManager, times(1))
                .sendMessage(any(), any(AcceptNotificationResponse.class));
    }

    @Test
    @DisplayName("Accept 실패 시, 실패 메시지를 전달한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long acceptorId = 1L;
        long inviterId = 2L;

        attributes.put(Constants.USER_ID.getValue(), acceptorId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userConnectionService.accept(eq(acceptorId), anyString()))
                .thenReturn(Pair.of(Optional.empty(), "errorMessage"));

        acceptRequestHandler.handle(new AcceptRequest("inviter"), mockSession);

        verify(sessionManager, never())
                .sendMessage(eq(mockSession), any(AcceptResponse.class));
        verify(sessionManager, never())
                .sendMessage(any(), any(AcceptNotificationResponse.class));
        verify(sessionManager, never())
                .findSessionByUserId(eq(inviterId));
        verify(sessionManager, times(1))
                .sendMessage(eq(mockSession), any(ErrorResponse.class));
    }
}