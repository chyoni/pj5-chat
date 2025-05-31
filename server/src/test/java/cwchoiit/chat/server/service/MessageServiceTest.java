package cwchoiit.chat.server.service;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.entity.Message;
import cwchoiit.chat.server.handler.response.MessageResponse;
import cwchoiit.chat.server.repository.MessageRepository;
import cwchoiit.chat.server.service.response.ChannelParticipantResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    MessageRepository messageRepository;
    @MockitoSpyBean
    ChannelService channelService;
    @MockitoSpyBean
    WebSocketSessionManager sessionManager;

    @Autowired
    MessageService messageService;

    @Test
    @DisplayName("sendMessage() 흐름 테스트 - Sender와 Partner가 동일한 유저인 경우, 아무런 행동도 하지 않는다.")
    void sendMessage() {
        when(userService.findUsernameByUserId(eq(1L)))
                .thenReturn(Optional.of("sender"));

        ChannelParticipantResponse sender = new ChannelParticipantResponse(1L);
        ChannelParticipantResponse partner = new ChannelParticipantResponse(1L);
        when(channelService.findParticipantIds(eq(1L)))
                .thenReturn(List.of(sender, partner));

        messageService.sendMessage(1L, 1L, "test");

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(messageRepository, times(1)).save(messageCaptor.capture());

        Message value = messageCaptor.getValue();
        assertThat(value.getUserId()).isEqualTo(1L);
        assertThat(value.getContent()).isEqualTo("test");

        verify(channelService, times(0)).isOnline(eq(1L), eq(1L));
        verify(sessionManager, times(0)).findSessionByUserId(anyLong());
        verify(sessionManager, times(0)).sendMessage(any(), any());
    }

    @Test
    @DisplayName("sendMessage() 흐름 테스트 - Sender와 Partner가 동일하지 않으면, Partner 에게 메시지를 전송하지만, Partner 세션이 끊어진 경우 전송하지 못한다.")
    void sendMessage3() {
        Long senderId = 1L;
        Long partnerId = 2L;
        long channelId = 1L;

        when(userService.findUsernameByUserId(eq(senderId)))
                .thenReturn(Optional.of("sender"));

        when(sessionManager.findSessionByUserId(eq(partnerId))).thenReturn(null);

        when(channelService.findOnlineParticipantIds(eq(channelId)))
                .thenReturn(List.of(senderId, partnerId));

        messageService.sendMessage(channelId, senderId, "test");

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(messageRepository, times(1)).save(messageCaptor.capture());

        Message value = messageCaptor.getValue();
        assertThat(value.getUserId()).isEqualTo(senderId);
        assertThat(value.getContent()).isEqualTo("test");
        verify(channelService, times(1)).findOnlineParticipantIds(eq(channelId));

        // 비동기 코드를 테스트 하기 위해 1초동안 polling 하면서 테스트 수행
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sessionManager, times(1)).findSessionByUserId(eq(partnerId));
            verify(sessionManager, times(0)).sendMessage(any(), any(MessageResponse.class));
        });
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

        when(channelService.findOnlineParticipantIds(eq(channelId)))
                .thenReturn(List.of(senderId, partnerId));

        when(sessionManager.findSessionByUserId(eq(partnerId))).thenReturn(mockSession);

        messageService.sendMessage(channelId, senderId, "test");

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(messageRepository, times(1)).save(messageCaptor.capture());

        Message value = messageCaptor.getValue();
        assertThat(value.getUserId()).isEqualTo(senderId);
        assertThat(value.getContent()).isEqualTo("test");

        verify(sessionManager, times(1)).findSessionByUserId(eq(partnerId));
        verify(sessionManager, times(1)).sendMessage(eq(mockSession), any(MessageResponse.class));
    }
}