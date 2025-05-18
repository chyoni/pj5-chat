package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.*;
import cwchoiit.chat.server.handler.response.ChannelJoinNotificationResponse;
import cwchoiit.chat.server.handler.response.CreateChannelResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.UserService;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cwchoiit.chat.server.constants.ChannelResponse.FAILED;
import static cwchoiit.chat.server.constants.ChannelResponse.SUCCESS;
import static cwchoiit.chat.server.constants.MessageType.CHANNEL_CREATE_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - CreateChannelRequestHandler")
class CreateChannelRequestHandlerTest {

    @Mock
    ChannelService channelService;
    @Mock
    UserService userService;
    @Mock
    WebSocketSessionManager sessionManager;
    @InjectMocks
    CreateChannelRequestHandler createChannelRequestHandler;

    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(CreateChannelRequestHandler.class);
    }

    @AfterEach
    void tearDown() {
        logCaptor.close();
    }

    @Test
    @DisplayName("CreateChannelRequestHandler는 MessageType이 [CHANNEL_CREATE_REQUEST]일때 처리할 수 있다.")
    void messageType() {
        String messageType = createChannelRequestHandler.messageType();
        assertThat(messageType).isEqualTo(CHANNEL_CREATE_REQUEST);
    }

    @Test
    @DisplayName("CreateChannelRequestHandler는 BaseRequest 인스턴스 타입이 CreateChannelRequest 일때 처리할 수 있다.")
    void handle() {
        createChannelRequestHandler.handle(new InviteRequest("123"), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new MessageRequest("123", "123"), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new FetchConnectionsRequest(PENDING), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new FetchUserInviteCodeRequest(), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new RejectRequest("inviter"), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new DisconnectRequest("inviter"), mock(WebSocketSession.class));
        createChannelRequestHandler.handle(new EnterChannelRequest(1L), mock(WebSocketSession.class));

        verify(userService, never()).findUserIdByUsername(anyString());
        verify(channelService, never()).createDirectChannel(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("create channel 로직이 정상 수행된다.")
    void handle_success() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        long channelId = 1L;
        long participantId = 2L;
        String participantUsername = "participant";
        String channelTitle = "channelTitle";
        ChannelCreateResponse channelCreateResponse = new ChannelCreateResponse(channelId, channelTitle, 2);

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdByUsername(eq(participantUsername)))
                .thenReturn(Optional.of(participantId));

        when(channelService.createDirectChannel(eq(requestUserId), eq(participantId), eq(channelTitle)))
                .thenReturn(Pair.of(Optional.of(channelCreateResponse), SUCCESS));


        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participantUsername), mockSession);

        ArgumentCaptor<CreateChannelResponse> captor = ArgumentCaptor.forClass(CreateChannelResponse.class);
        verify(sessionManager, times(1)).sendMessage(eq(mockSession), captor.capture());
        assertThat(captor.getValue().getChannelId()).isEqualTo(channelId);
        assertThat(captor.getValue().getTitle()).isEqualTo(channelTitle);

        ArgumentCaptor<ChannelJoinNotificationResponse> channelJoinCaptor = ArgumentCaptor.forClass(ChannelJoinNotificationResponse.class);
        verify(sessionManager, times(1)).sendMessage(any(), channelJoinCaptor.capture());
        verify(sessionManager, times(1)).findSessionByUserId(eq(participantId));
        assertThat(channelJoinCaptor.getValue().getChannelId()).isEqualTo(channelId);
        assertThat(channelJoinCaptor.getValue().getTitle()).isEqualTo(channelTitle);

        verify(sessionManager, never()).sendMessage(eq(mockSession), any(ErrorResponse.class));
    }

    @Test
    @DisplayName("create channel 로직이 실패하면 에러 메시지를 전송한다.")
    void handle_failed2() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        long participantId = 2L;
        String participantUsername = "participant";
        String channelTitle = "channelTitle";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdByUsername(eq(participantUsername)))
                .thenReturn(Optional.of(participantId));

        when(channelService.createDirectChannel(eq(requestUserId), eq(participantId), eq(channelTitle)))
                .thenReturn(Pair.of(Optional.empty(), FAILED));


        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participantUsername), mockSession);

        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(ErrorResponse.class));
        verify(sessionManager, never()).sendMessage(any(), any(ChannelJoinNotificationResponse.class));
        verify(sessionManager, never()).sendMessage(eq(mockSession), any(CreateChannelResponse.class));
    }

    @Test
    @DisplayName("다이렉트 메시지 대상자가 없는 경우, 에러 메시지를 반환한다.")
    void handle_not_found_participant() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        String participantUsername = "participant";
        String channelTitle = "channelTitle";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdByUsername(eq(participantUsername)))
                .thenReturn(Optional.empty());

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participantUsername), mockSession);

        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(ErrorResponse.class));
        verify(channelService, never()).createDirectChannel(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("다이렉트 채널 생성 중, 예외가 발생하면 채널 생성에 실패하고 예외 메시지를 반환한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        long participantId = 2L;
        String participantUsername = "participant";
        String channelTitle = "channelTitle";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdByUsername(eq(participantUsername)))
                .thenReturn(Optional.of(participantId));

        doThrow(new RuntimeException("test exception"))
                .when(channelService)
                .createDirectChannel(eq(requestUserId), eq(participantId), eq(channelTitle));

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participantUsername), mockSession);

        assertThat(logCaptor.getErrorLogs()).hasSize(1);
        assertThat(logCaptor.getErrorLogs())
                .anyMatch(log -> log.contains("[handle] Exception occurred while creating channel."));
        verify(sessionManager, times(1))
                .sendMessage(eq(mockSession), any(ErrorResponse.class));
    }
}