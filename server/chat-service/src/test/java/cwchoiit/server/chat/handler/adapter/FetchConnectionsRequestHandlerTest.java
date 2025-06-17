package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.MessageType;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.FetchConnectionsResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import cwchoiit.server.chat.service.response.UserReadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - FetchConnectionsRequestHandler")
class FetchConnectionsRequestHandlerTest {

    @Mock
    UserConnectionService userConnectionService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    FetchConnectionsRequestHandler fetchConnectionsRequestHandler;

    @Test
    @DisplayName("FetchConnectionsRequestHandler MessageType이 [FETCH_CONNECTIONS_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = fetchConnectionsRequestHandler.messageType();
        assertThat(messageType).isEqualTo(MessageType.FETCH_CONNECTIONS_REQUEST);
    }

    @Test
    @DisplayName("FetchConnectionsRequestHandler BaseRequest 인스턴스 타입이 FetchConnectionsRequest 일때 처리할 수 있다.")
    void handle() {
        fetchConnectionsRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        fetchConnectionsRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));
        fetchConnectionsRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        fetchConnectionsRequestHandler.handle(new MessageRequest(1L, "message"), mock(WebSocketSession.class));
        fetchConnectionsRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        fetchConnectionsRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));

        verify(userConnectionService, never()).findConnectionUsersByStatus(anyLong(), any());
    }

    @Test
    @DisplayName("FetchConnectionsRequest가 들어오면, 로직이 수행된다 - 정상 케이스")
    void handle_success() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);

        FetchConnectionsRequest fetchConnectionsRequest = new FetchConnectionsRequest(UserConnectionStatus.ACCEPTED);
        when(userConnectionService.findConnectionUsersByStatus(eq(1L), eq(fetchConnectionsRequest.getStatus())))
                .thenReturn(
                        List.of(
                                new UserReadResponse(1L, "username"),
                                new UserReadResponse(2L, "username2"),
                                new UserReadResponse(3L, "username3")
                        )
                );

        fetchConnectionsRequestHandler.handle(fetchConnectionsRequest, mocked);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mocked), eq(1L), any(FetchConnectionsResponse.class));
    }
}