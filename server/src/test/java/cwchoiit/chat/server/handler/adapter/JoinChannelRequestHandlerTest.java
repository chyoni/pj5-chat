package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.JoinChannelResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
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

import static cwchoiit.chat.server.constants.MessageType.JOIN_CHANNEL_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - JoinChannelRequestHandler")
class JoinChannelRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    WebSocketSessionManager sessionManager;
    @InjectMocks
    JoinChannelRequestHandler joinChannelRequestHandler;

    @Test
    @DisplayName("JoinChannelRequestHandler MessageType이 [JOIN_CHANNEL_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = joinChannelRequestHandler.messageType();
        assertThat(messageType).isEqualTo(JOIN_CHANNEL_REQUEST);
    }

    @Test
    @DisplayName("JoinChannelRequestHandler BaseRequest 인스턴스 타입이 JoinChannelRequest 일때 처리할 수 있다.")
    void handle() {
        joinChannelRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new CreateChannelRequest("title", List.of("participant")), mock(WebSocketSession.class));
        joinChannelRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));

        verify(channelService, never()).enter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("채널 조인 시, 예외가 발생하면, 예외 메시지를 전달한다.")
    void handle_exception() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        doThrow(new RuntimeException("test exception"))
                .when(channelService).join(any(), eq(callerId));

        joinChannelRequestHandler.handle(new JoinChannelRequest("abcd"), mockSession);

        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(ErrorResponse.class));
        verify(sessionManager, never()).sendMessage(eq(mockSession), any(JoinChannelResponse.class));
    }

    @Test
    @DisplayName("채널 조인 시, 조인에 실패하면, 실패 메시지를 전달한다.")
    void handle_failed() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        String inviteCode = "inviteCode";
        when(channelService.join(eq(inviteCode), eq(callerId)))
                .thenReturn(Pair.of(Optional.empty(), ChannelResponse.INVALID_ARGS));

        joinChannelRequestHandler.handle(new JoinChannelRequest(inviteCode), mockSession);

        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(ErrorResponse.class));
        verify(sessionManager, never()).sendMessage(eq(mockSession), any(JoinChannelResponse.class));
    }

    @Test
    @DisplayName("채널 조인 시, 조인에 성공하면, 성공 메시지를 전달한다.")
    void handle_success() {
        long callerId = 1L;
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), callerId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        String inviteCode = "inviteCode";
        when(channelService.join(eq(inviteCode), eq(callerId)))
                .thenReturn(Pair.of(
                                Optional.of(new ChannelReadResponse(1L, "ch", 3)),
                                ChannelResponse.SUCCESS
                        )
                );

        joinChannelRequestHandler.handle(new JoinChannelRequest(inviteCode), mockSession);

        verify(sessionManager, never()).sendMessage(eq(mockSession), any(ErrorResponse.class));
        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(JoinChannelResponse.class));
    }
}