package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.FetchChannelInviteCodeResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.ClientNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - FetchChannelInviteCodeRequestHandler")
class FetchChannelInviteCodeRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    ClientNotificationService clientNotificationService;
    @InjectMocks
    FetchChannelInviteCodeRequestHandler fetchChannelInviteCodeRequestHandler;

    @Test
    @DisplayName("FetchChannelInviteCodeRequestHandler MessageType이 [FETCH_CHANNEL_INVITE_CODE_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = fetchChannelInviteCodeRequestHandler.messageType();
        assertThat(messageType).isEqualTo(FETCH_CHANNEL_INVITE_CODE_REQUEST);
    }

    @Test
    @DisplayName("FetchChannelInviteCodeRequestHandler BaseRequest 인스턴스 타입이 FetchChannelInviteCodeRequest 일때 처리할 수 있다.")
    void handle() {
        fetchChannelInviteCodeRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        fetchChannelInviteCodeRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("초대 코드 요청자가 채널에 조인된 상태가 아니라면, 예외 메시지를 전달한다.")
    void handle_error() {
        long channelId = 1L;
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.isJoined(channelId, callerId)).thenReturn(false);

        fetchChannelInviteCodeRequestHandler.handle(new FetchChannelInviteCodeRequest(channelId), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
        verify(channelService, never()).findInviteCode(anyLong());
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(FetchChannelInviteCodeResponse.class));
    }

    @Test
    @DisplayName("초대 코드 요청자가 채널에 조인된 상태이지만, 채널의 채널 코드를 찾지 못했을 때, 예외 메시지를 전달한다.")
    void handle_error_not_found() {
        long channelId = 1L;
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.isJoined(channelId, callerId)).thenReturn(true);
        when(channelService.findInviteCode(channelId)).thenReturn(Optional.empty());

        fetchChannelInviteCodeRequestHandler.handle(new FetchChannelInviteCodeRequest(channelId), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(FetchChannelInviteCodeResponse.class));
    }

    @Test
    @DisplayName("초대 코드 요청자가 채널에 조인된 상태이고, 채널 코드를 정상적으로 가지고 있다면 정상 로직이 수행된다.")
    void handle_success() {
        long channelId = 1L;
        long callerId = 1L;

        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(channelService.isJoined(channelId, callerId)).thenReturn(true);
        when(channelService.findInviteCode(channelId)).thenReturn(Optional.of("inviteCode"));

        fetchChannelInviteCodeRequestHandler.handle(new FetchChannelInviteCodeRequest(channelId), mockSession);

        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(callerId), any(ErrorResponse.class));

        ArgumentCaptor<FetchChannelInviteCodeResponse> captor = ArgumentCaptor.forClass(FetchChannelInviteCodeResponse.class);
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(callerId), captor.capture());

        FetchChannelInviteCodeResponse value = captor.getValue();
        assertThat(value.getInviteCode()).isEqualTo("inviteCode");
        assertThat(value.getChannelId()).isEqualTo(channelId);
    }
}