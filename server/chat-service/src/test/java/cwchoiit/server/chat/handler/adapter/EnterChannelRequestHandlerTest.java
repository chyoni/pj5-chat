package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.EnterChannelResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cwchoiit.server.chat.constants.ChannelResponse.FAILED;
import static cwchoiit.server.chat.constants.ChannelResponse.SUCCESS;
import static cwchoiit.server.chat.constants.MessageType.ENTER_CHANNEL_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - EnterChannelRequestHandler")
class EnterChannelRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    EnterChannelRequestHandler enterChannelRequestHandler;

    @Test
    @DisplayName("EnterChannelRequestHandler MessageType이 [ENTER_CHANNEL_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = enterChannelRequestHandler.messageType();
        assertThat(messageType).isEqualTo(ENTER_CHANNEL_REQUEST);
    }

    @Test
    @DisplayName("EnterChannelRequestHandler는 BaseRequest 인스턴스 타입이 EnterChannelRequest 일때 처리할 수 있다.")
    void handle() {
        enterChannelRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        enterChannelRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("enter 로직이 정상 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        long channelId = 1L;

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.enter(eq(requestUserId), eq(channelId)))
                .thenReturn(Pair.of(Optional.of("title"), SUCCESS));

        enterChannelRequestHandler.handle(new EnterChannelRequest(channelId), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(requestUserId), any(EnterChannelResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("enter 실패 시, 실패 메시지를 전달한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        long channelId = 1L;

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.enter(eq(requestUserId), eq(channelId)))
                .thenReturn(Pair.of(Optional.empty(), FAILED));

        enterChannelRequestHandler.handle(new EnterChannelRequest(channelId), mockSession);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(requestUserId), any(EnterChannelResponse.class));
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
    }
}