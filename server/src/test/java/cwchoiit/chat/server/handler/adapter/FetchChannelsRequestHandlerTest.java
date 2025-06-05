package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.FetchChannelsResponse;
import cwchoiit.chat.server.service.ChannelService;

import cwchoiit.chat.server.service.ClientNotificationService;
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

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNELS_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - FetchChannelsRequestHandler")
class FetchChannelsRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    FetchChannelsRequestHandler fetchChannelsRequestHandler;

    @Test
    @DisplayName("FetchChannelsRequestHandler MessageType이 [FETCH_CHANNELS_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = fetchChannelsRequestHandler.messageType();
        assertThat(messageType).isEqualTo(FETCH_CHANNELS_REQUEST);
    }

    @Test
    @DisplayName("FetchChannelsRequestHandler BaseRequest 인스턴스 타입이 FetchChannelsRequest 일때 처리할 수 있다.")
    void handle() {
        fetchChannelsRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        fetchChannelsRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("정상 로직이 수행된다.")
    void handle2() {
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        fetchChannelsRequestHandler.handle(new FetchChannelsRequest(), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(FetchChannelsResponse.class));
    }
}