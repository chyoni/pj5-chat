package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.constants.MessageType;
import cwchoiit.server.chat.handler.response.MessageResponse;
import cwchoiit.server.chat.session.WebSocketSessionManager;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Service - ClientNotificationService")
@ExtendWith(MockitoExtension.class)
class ClientNotificationServiceTest {

    @Mock
    WebSocketSessionManager sessionManager;
    @Mock
    PushService pushService;
    @InjectMocks
    ClientNotificationService clientNotificationService;

    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(ClientNotificationService.class);
    }

    @AfterEach
    void tearDown() {
        logCaptor.close();
    }

    @Test
    @DisplayName("전달할 메시지를 직렬화하지 못하면, 로그를 출력하고 메서드를 리턴한다.")
    void sendPayload() throws IOException {
        WebSocketSession mockSession = mock(WebSocketSession.class);

        try (MockedStatic<Serializer> mockedSerializer = Mockito.mockStatic(Serializer.class)) {
            mockedSerializer.when(() -> Serializer.serialize(any())).thenReturn(Optional.empty());
            MessageResponse messageResponse = new MessageResponse(1L, "Invalid", "Content");
            clientNotificationService.sendMessage(mockSession, 1L, messageResponse);

            assertThat(logCaptor.getErrorLogs()).anyMatch(log -> log.contains("Send message failed."));
            verify(sessionManager, never()).sendMessage(eq(mockSession), any());
        }
    }

    @Test
    @DisplayName("전달할 메시지를 전송하는 중 예외가 발생하면, 푸시 알림으로 대체한다.")
    void sendPayloadWithException() throws IOException {
        WebSocketSession mockSession = mock(WebSocketSession.class);

        MessageResponse messageResponse = new MessageResponse(1L, "Invalid", "Content");
        doThrow(IOException.class)
                .when(sessionManager)
                .sendMessage(eq(mockSession), any());

        clientNotificationService.sendMessage(mockSession, 1L, messageResponse);

        verify(pushService, times(1)).pushMessage(eq(1L), eq(MessageType.MESSAGE), any());
    }
}