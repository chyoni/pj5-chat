package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.MessageType;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.RejectResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - RejectRequestHandler")
class RejectRequestHandlerTest {

    @Mock
    UserConnectionService userConnectionService;

    @Mock
    ClientNotificationService clientNotificationService;

    @InjectMocks
    RejectRequestHandler rejectRequestHandler;

    @Test
    @DisplayName("RejectRequestHandler는 MessageType이 [REJECT_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = rejectRequestHandler.messageType();
        assertThat(messageType).isEqualTo(MessageType.REJECT_REQUEST);
    }

    @Test
    @DisplayName("RejectRequestHandler는 BaseRequest 인스턴스 타입이 RejectRequest 일때 처리할 수 있다.")
    void handle() {
        rejectRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        rejectRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));
        rejectRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        rejectRequestHandler.handle(new MessageRequest(1L, "message"), mock(WebSocketSession.class));
        rejectRequestHandler.handle(new FetchConnectionsRequest(UserConnectionStatus.REJECTED), mock(WebSocketSession.class));
        rejectRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));

        verify(userConnectionService, never()).reject(anyLong(), anyString());
    }

    @Test
    @DisplayName("RejectRequest가 들어오면, 로직이 수행된다 - 정상 케이스")
    void handle_success() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);

        RejectRequest rejectRequest = new RejectRequest("inviter");
        when(userConnectionService.reject(eq(1L), eq(rejectRequest.getInviterUsername())))
                .thenReturn(Pair.of(true, rejectRequest.getInviterUsername()));

        rejectRequestHandler.handle(rejectRequest, mocked);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mocked), eq(1L), any(RejectResponse.class));
    }

    @Test
    @DisplayName("RejectRequest가 들어오면, 로직이 수행된다 - 실패 케이스")
    void handle_failed() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);

        RejectRequest rejectRequest = new RejectRequest("inviter");
        when(userConnectionService.reject(eq(1L), eq(rejectRequest.getInviterUsername())))
                .thenReturn(Pair.of(false, "Error Message"));

        rejectRequestHandler.handle(rejectRequest, mocked);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mocked), eq(1L), any(ErrorResponse.class));
    }
}