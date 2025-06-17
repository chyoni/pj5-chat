package cwchoiit.server.chat.handler.adapter;


import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.LeaveChannelResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
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

import static cwchoiit.server.chat.constants.MessageType.LEAVE_CHANNEL_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - LeaveChannelRequestHandler")
class LeaveChannelRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    LeaveChannelRequestHandler leaveChannelRequestHandler;

    @Test
    @DisplayName("LeaveChannelRequestHandler MessageType이 [LEAVE_CHANNEL_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = leaveChannelRequestHandler.messageType();
        assertThat(messageType).isEqualTo(LEAVE_CHANNEL_REQUEST);
    }

    @Test
    @DisplayName("LeaveChannelRequestHandler BaseRequest 인스턴스 타입이 LeaveChannelRequest 일때 처리할 수 있다.")
    void handle() {
        leaveChannelRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        leaveChannelRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("채널을 떠나기에 성공하는 경우, 안내 메시지를 전달한다.")
    void handle2() {
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.leave(callerId)).thenReturn(true);

        leaveChannelRequestHandler.handle(new LeaveChannelRequest(), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(LeaveChannelResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("채널을 떠나기에 실패하는 경우, 에러 메시지를 전달한다.")
    void handle3() {
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.leave(callerId)).thenReturn(false);

        leaveChannelRequestHandler.handle(new LeaveChannelRequest(), mockSession);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(LeaveChannelResponse.class));
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
    }
}