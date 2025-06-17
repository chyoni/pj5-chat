package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.entity.Message;
import cwchoiit.server.chat.service.response.ChannelParticipantResponse;
import cwchoiit.server.chat.session.WebSocketSessionManager;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cwchoiit.server.chat.constants.MessageType.MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Service - MessageService")
class MessageServiceTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    UserService userService;
    @MockitoSpyBean
    MessageCommandService messageCommandService;
    @MockitoSpyBean
    ChannelService channelService;
    @MockitoSpyBean
    PushService pushService;
    @MockitoSpyBean
    WebSocketSessionManager sessionManager;

    @Autowired
    MessageService messageService;

    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(MessageService.class);
    }

    @AfterEach
    void tearDown() {
        logCaptor.close();
    }

    @Test
    @DisplayName("sendMessage() 흐름 테스트 - Sender와 Partner가 동일한 유저인 경우, 아무런 행동도 하지 않는다.")
    void sendMessage() throws IOException {
        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("sender"));

        ChannelParticipantResponse sender = new ChannelParticipantResponse(1L);
        ChannelParticipantResponse partner = new ChannelParticipantResponse(1L);
        when(channelService.findParticipantIds(eq(1L)))
                .thenReturn(List.of(sender, partner));

        messageService.sendMessage(1L, 1L, "test");

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(messageCommandService, times(1)).saveMessage(messageCaptor.capture());

        Message value = messageCaptor.getValue();
        assertThat(value.getUserId()).isEqualTo(1L);
        assertThat(value.getContent()).isEqualTo("test");

        verify(channelService, times(0)).isOnline(eq(1L), eq(1L));
        verify(sessionManager, times(0)).findSessionByUserId(anyLong());
        verify(sessionManager, times(0)).sendMessage(any(), any());
    }

    @Test
    @DisplayName("메시지 전달 과정에서 메시지 직렬화에 실패하면, 예외 로그를 찍고 아무것도 하지 않는다.")
    void sendMessageWithError() {
        try (MockedStatic<Serializer> mockedSerializer = mockStatic(Serializer.class)) {
            mockedSerializer.when(() -> Serializer.serialize(any())).thenReturn(Optional.empty());

            doReturn(Optional.of("sender")).when(userService).findUsernameByUserId(eq(1L));

            messageService.sendMessage(1L, 1L, "test");

            assertThat(logCaptor.getErrorLogs()).anyMatch(log -> log.contains("Send message failed"));
            verify(messageCommandService, never()).saveMessage(any());
        }
    }

    @Test
    @DisplayName("sendMessage() 흐름 테스트 - Sender와 Partner가 동일하지 않으면, Partner 에게 메시지를 전달한다.")
    void sendMessage2() {
        Long senderId = 1L;
        Long partnerId = 2L;
        long channelId = 1L;

        when(userService.findUsernameByUserId(eq(senderId)))
                .thenReturn(Optional.of("sender"));

        WebSocketSession mockSession = mock(WebSocketSession.class);

        doReturn(List.of(new ChannelParticipantResponse(partnerId), new ChannelParticipantResponse(senderId)))
                .when(channelService).findParticipantIds(eq(channelId));

        doReturn(List.of(partnerId, senderId))
                .when(channelService).findOnlineParticipantIds(eq(channelId), eq(List.of(partnerId, senderId)));

        when(sessionManager.findSessionByUserId(eq(partnerId))).thenReturn(mockSession);

        messageService.sendMessage(channelId, senderId, "test");

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(messageCommandService, times(1)).saveMessage(messageCaptor.capture());

        Message value = messageCaptor.getValue();
        assertThat(value.getUserId()).isEqualTo(senderId);
        assertThat(value.getContent()).isEqualTo("test");

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sessionManager, times(1)).findSessionByUserId(eq(partnerId));
            verify(sessionManager, times(1)).sendMessage(eq(mockSession), any());
        });
    }

    @Test
    @DisplayName("온라인 참여자 ID중 null값이 나온 경우, 푸시 알림으로 대체한다.")
    void sendMessage3() {
        Long senderId = 1L;
        Long partnerId = 2L;
        long channelId = 1L;

        when(userService.findUsernameByUserId(eq(senderId)))
                .thenReturn(Optional.of("sender"));


        doReturn(List.of(new ChannelParticipantResponse(partnerId), new ChannelParticipantResponse(senderId)))
                .when(channelService).findParticipantIds(eq(channelId));

        List<Long> onlineParticipantIds = new ArrayList<>();
        onlineParticipantIds.add(null);

        doReturn(onlineParticipantIds)
                .when(channelService).findOnlineParticipantIds(eq(channelId), eq(List.of(partnerId, senderId)));

        messageService.sendMessage(channelId, senderId, "test");

        verify(pushService, times(1)).pushMessage(eq(2L), eq(MESSAGE), any());
    }

    @Test
    @DisplayName("온라인 참여자 ID중 ID로 세션을 구한 세션값이 null인 경우, 푸시 알림으로 대체한다.")
    void sendMessage4() {
        Long senderId = 1L;
        Long partnerId = 2L;
        long channelId = 1L;

        when(userService.findUsernameByUserId(eq(senderId)))
                .thenReturn(Optional.of("sender"));

        WebSocketSession mockSession = mock(WebSocketSession.class);

        doReturn(List.of(new ChannelParticipantResponse(partnerId), new ChannelParticipantResponse(senderId)))
                .when(channelService).findParticipantIds(eq(channelId));

        List<Long> onlineParticipantIds = new ArrayList<>();
        onlineParticipantIds.add(partnerId);
        onlineParticipantIds.add(senderId);

        doReturn(onlineParticipantIds)
                .when(channelService).findOnlineParticipantIds(eq(channelId), eq(List.of(partnerId, senderId)));

        doReturn(null)
                .when(sessionManager)
                .findSessionByUserId(eq(partnerId));

        messageService.sendMessage(channelId, senderId, "test");

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sessionManager, never()).sendMessage(eq(mockSession), any());
            verify(pushService, times(1)).pushMessage(eq(2L), eq(MESSAGE), any());
        });
    }

    @Test
    @DisplayName("메시지를 전달하는 중 예외가 발생하면, 푸시 알림으로 대체한다.")
    void sendMessage5() {
        Long senderId = 1L;
        Long partnerId = 2L;
        long channelId = 1L;

        when(userService.findUsernameByUserId(eq(senderId)))
                .thenReturn(Optional.of("sender"));

        WebSocketSession mockSession = mock(WebSocketSession.class);

        doReturn(List.of(new ChannelParticipantResponse(partnerId), new ChannelParticipantResponse(senderId)))
                .when(channelService).findParticipantIds(eq(channelId));

        List<Long> onlineParticipantIds = new ArrayList<>();
        onlineParticipantIds.add(partnerId);
        onlineParticipantIds.add(senderId);

        doReturn(onlineParticipantIds)
                .when(channelService).findOnlineParticipantIds(eq(channelId), eq(List.of(partnerId, senderId)));

        doThrow(new RuntimeException("test exception"))
                .when(sessionManager)
                .findSessionByUserId(eq(partnerId));

        messageService.sendMessage(channelId, senderId, "test");

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sessionManager, never()).sendMessage(eq(mockSession), any());
            verify(pushService, times(1)).pushMessage(eq(2L), eq(MESSAGE), any());
        });
    }
}