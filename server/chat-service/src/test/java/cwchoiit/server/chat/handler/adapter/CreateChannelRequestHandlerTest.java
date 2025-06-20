package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.*;
import cwchoiit.server.chat.handler.response.ChannelJoinNotificationResponse;
import cwchoiit.server.chat.handler.response.CreateChannelResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserService;
import cwchoiit.server.chat.service.response.ChannelCreateResponse;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

import static cwchoiit.server.chat.constants.ChannelResponse.INVALID_ARGS;
import static cwchoiit.server.chat.constants.ChannelResponse.SUCCESS;
import static cwchoiit.server.chat.constants.MessageType.CHANNEL_CREATE_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
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
    ClientNotificationService clientNotificationService;
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
        createChannelRequestHandler.handle(new MessageRequest(1L, "123"), mock(WebSocketSession.class));
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
        String channelTitle = "channelTitle";
        List<String> participants = List.of("participants1", "participants2");

        ChannelCreateResponse channelCreateResponse = new ChannelCreateResponse(channelId, channelTitle, 2);

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdsByUsernames(eq(participants)))
                .thenReturn(List.of(2L, 3L));

        when(channelService.createGroupChannel(eq(requestUserId), eq(List.of(2L, 3L)), eq(channelTitle)))
                .thenReturn(Pair.of(Optional.of(channelCreateResponse), SUCCESS));

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participants), mockSession);

        ArgumentCaptor<CreateChannelResponse> captor = ArgumentCaptor.forClass(CreateChannelResponse.class);
        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(requestUserId), captor.capture());
        assertThat(captor.getValue().getChannelId()).isEqualTo(channelId);
        assertThat(captor.getValue().getTitle()).isEqualTo(channelTitle);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(clientNotificationService, times(1)).sendMessage(eq(2L), any(ChannelJoinNotificationResponse.class));
            verify(clientNotificationService, times(1)).sendMessage(eq(3L), any(ChannelJoinNotificationResponse.class));

            verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
        });
    }

    @Test
    @DisplayName("create channel 로직이 실패하면 에러 메시지를 전송한다.")
    void handle_failed2() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        List<String> participants = List.of("participants1", "participants2");
        String channelTitle = "channelTitle";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdsByUsernames(eq(participants)))
                .thenReturn(List.of(2L, 3L));

        when(channelService.createGroupChannel(eq(requestUserId), eq(List.of(2L, 3L)), eq(channelTitle)))
                .thenReturn(Pair.of(Optional.empty(), INVALID_ARGS));

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participants), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
        verify(clientNotificationService, never()).sendMessage(any(), any(ChannelJoinNotificationResponse.class));
        verify(clientNotificationService, never()).sendMessage(eq(mockSession), eq(requestUserId), any(CreateChannelResponse.class));
    }

    @Test
    @DisplayName("다이렉트 메시지 대상자가 없는 경우, 에러 메시지를 반환한다.")
    void handle_not_found_participant() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        String channelTitle = "channelTitle";
        List<String> participants = List.of("participants1", "participants2");

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdsByUsernames(eq(participants)))
                .thenReturn(Collections.emptyList());

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participants), mockSession);

        verify(clientNotificationService, times(1)).sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
        verify(channelService, never()).createGroupChannel(anyLong(), anyList(), anyString());
    }

    @Test
    @DisplayName("다이렉트 채널 생성 중, 예외가 발생하면 채널 생성에 실패하고 예외 메시지를 반환한다.")
    void handle_failed() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();

        long requestUserId = 1L;
        List<String> participants = List.of("participants1", "participants2");
        String channelTitle = "channelTitle";

        attributes.put(IdKey.USER_ID.getValue(), requestUserId);
        when(mockSession.getAttributes()).thenReturn(attributes);

        when(userService.findUserIdsByUsernames(eq(participants)))
                .thenReturn(List.of(2L, 3L));

        doThrow(new RuntimeException("test exception"))
                .when(channelService)
                .createGroupChannel(eq(requestUserId), eq(List.of(2L, 3L)), eq(channelTitle));

        createChannelRequestHandler.handle(new CreateChannelRequest(channelTitle, participants), mockSession);

        assertThat(logCaptor.getErrorLogs()).hasSize(1);
        assertThat(logCaptor.getErrorLogs())
                .anyMatch(log -> log.contains("[handle] Exception occurred while creating channel."));
        verify(clientNotificationService, times(1))
                .sendMessage(eq(mockSession), eq(requestUserId), any(ErrorResponse.class));
    }
}