package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.QuitChannelResponse;
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

import static cwchoiit.chat.server.constants.MessageType.QUIT_CHANNEL_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - QuitChannelRequestHandler")
class QuitChannelRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    QuitChannelRequestHandler quitChannelRequestHandler;

    @Test
    @DisplayName("QuitChannelRequestHandler MessageType이 [QUIT_CHANNEL_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = quitChannelRequestHandler.messageType();
        assertThat(messageType).isEqualTo(QUIT_CHANNEL_REQUEST);
    }

    @Test
    @DisplayName("QuitChannelRequestHandler BaseRequest 인스턴스 타입이 QuitChannelRequest 일때 처리할 수 있다.")
    void handle() {
        quitChannelRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        quitChannelRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("채널 삭제 시, 예외가 발생하면, 예외 메시지를 전달한다.")
    void handle_exception() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        long channelId = 100L;
        doThrow(new RuntimeException("test exception"))
                .when(channelService).quit(eq(channelId), eq(callerId));

        quitChannelRequestHandler.handle(new QuitChannelRequest(channelId), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(QuitChannelResponse.class));
    }

    @Test
    @DisplayName("채널 삭제 시, 삭제에 실패하면, 실패 메시지를 전달한다.")
    void handle_failed() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        long channelId = 100L;

        when(channelService.quit(eq(channelId), eq(callerId)))
                .thenReturn(ChannelResponse.FAILED);
        quitChannelRequestHandler.handle(new QuitChannelRequest(channelId), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(QuitChannelResponse.class));
    }

    @Test
    @DisplayName("채널 삭제 시, 삭제에 성공하면, 성공 메시지를 전달한다.")
    void handle_success() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        long channelId = 100L;

        when(channelService.quit(eq(channelId), eq(callerId)))
                .thenReturn(ChannelResponse.SUCCESS);
        quitChannelRequestHandler.handle(new QuitChannelRequest(channelId), mockSession);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(QuitChannelResponse.class));
    }
}