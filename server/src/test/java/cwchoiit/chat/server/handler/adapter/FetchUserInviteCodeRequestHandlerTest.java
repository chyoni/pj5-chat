package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.constants.MessageType;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.FetchUserInviteCodeResponse;
import cwchoiit.chat.server.service.ClientNotificationService;
import cwchoiit.chat.server.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - FetchUserInviteCodeRequestHandler")
class FetchUserInviteCodeRequestHandlerTest {

    @Mock
    UserService userService;

    @Mock
    ClientNotificationService clientNotificationService;

    @InjectMocks
    FetchUserInviteCodeRequestHandler fetchUserInviteCodeRequestHandler;

    @Test
    @DisplayName("FetchUserInviteCodeRequestHandler MessageType이 [FETCH_USER_INVITE_CODE_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = fetchUserInviteCodeRequestHandler.messageType();
        assertThat(messageType).isEqualTo(MessageType.FETCH_USER_INVITE_CODE_REQUEST);
    }

    @Test
    @DisplayName("FetchUserInviteCodeRequestHandler BaseRequest 인스턴스 타입이 FetchUserInviteCodeRequest 일때 처리할 수 있다.")
    void handle() {
        fetchUserInviteCodeRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        fetchUserInviteCodeRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));
        fetchUserInviteCodeRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        fetchUserInviteCodeRequestHandler.handle(new MessageRequest(1L, "message"), mock(WebSocketSession.class));
        fetchUserInviteCodeRequestHandler.handle(new FetchConnectionsRequest(UserConnectionStatus.REJECTED), mock(WebSocketSession.class));
        fetchUserInviteCodeRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));

        verify(userService, never()).findInviteCodeByUserId(anyLong());
    }

    @Test
    @DisplayName("FetchUserInviteCodeRequest가 들어오면, 로직이 수행된다 - 정상 케이스")
    void handle_success() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);

        FetchUserInviteCodeRequest fetchUserInviteCodeRequest = new FetchUserInviteCodeRequest();
        when(userService.findInviteCodeByUserId(eq(1L)))
                .thenReturn(Optional.of("inviteCode"));

        fetchUserInviteCodeRequestHandler.handle(fetchUserInviteCodeRequest, mocked);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mocked), eq(1L), any(FetchUserInviteCodeResponse.class));
        verify(clientNotificationService, never())
                .sendMessage(eq(mocked), eq(1L), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("FetchUserInviteCodeRequest가 들어오면, 로직이 수행된다 - 실패 케이스")
    void handle_failed() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);

        FetchUserInviteCodeRequest fetchUserInviteCodeRequest = new FetchUserInviteCodeRequest();
        when(userService.findInviteCodeByUserId(eq(1L)))
                .thenReturn(Optional.empty());

        fetchUserInviteCodeRequestHandler.handle(fetchUserInviteCodeRequest, mocked);

        verify(clientNotificationService, times(1))
                .sendMessage(eq(mocked), eq(1L), any(ErrorResponse.class));
        verify(clientNotificationService, never())
                .sendMessage(eq(mocked), eq(1L), any(FetchUserInviteCodeResponse.class));
    }
}