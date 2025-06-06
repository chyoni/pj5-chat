package cwchoiit.chat.server.session;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.chat.server.handler.response.MessageResponse;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Session - WebSocketSessionManager")
class WebSocketSessionManagerTest {

    WebSocketSessionManager webSocketSessionManager;

    @BeforeEach
    void setUp() {
        webSocketSessionManager = new WebSocketSessionManager();
    }

    @Test
    @DisplayName("유저가 한명도 없는 경우 저장하고 있는 유저는 없다.")
    void webSocketSessionManager1() {
        List<WebSocketSession> sessions = webSocketSessionManager.getSessions();

        assertThat(sessions).isEmpty();
    }

    @Test
    @DisplayName("유저가 접속한 경우 해당 유저의 세션을 가져올 수 있다.")
    void webSocketSessionManager2() {
        webSocketSessionManager.storeSession(1L, mock(WebSocketSession.class));
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        List<WebSocketSession> sessions = webSocketSessionManager.getSessions();
        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("유저가 접속한 경우 해당 유저의 ID로 세션을 가져올 수 있다.")
    void webSocketSessionManager3() {
        webSocketSessionManager.storeSession(1L, mock(WebSocketSession.class));
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        WebSocketSession sessionByUserId = webSocketSessionManager.findSessionByUserId(1L);
        assertThat(sessionByUserId).isNotNull();
    }

    @Test
    @DisplayName("없는 유저의 ID로 세션을 가져올 수 없다.")
    void webSocketSessionManager4() {
        webSocketSessionManager.storeSession(1L, mock(WebSocketSession.class));
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        WebSocketSession sessionByUserId = webSocketSessionManager.findSessionByUserId(3L);
        assertThat(sessionByUserId).isNull();
    }

    @Test
    @DisplayName("세션을 저장이 정상적으로 수행된다.")
    void webSocketSessionManager5() {
        webSocketSessionManager.storeSession(1L, mock(WebSocketSession.class));
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        List<WebSocketSession> sessions = webSocketSessionManager.getSessions();
        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("세션 삭제가 정상적으로 수행된다.")
    void webSocketSessionManager6() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        webSocketSessionManager.storeSession(1L, mockSession);
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        webSocketSessionManager.terminateSession(1L);
        WebSocketSession deleted = webSocketSessionManager.findSessionByUserId(1L);

        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("세션 삭제 시, 이미 삭제된 세션이라면 아무 작업도 하지 않는다.")
    void webSocketSessionManager6_1() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        webSocketSessionManager.storeSession(1L, mockSession);
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        webSocketSessionManager.terminateSession(3L);

        WebSocketSession empty = webSocketSessionManager.findSessionByUserId(3L);
        assertThat(empty).isNull();
    }

    @Test
    @DisplayName("세션 삭제 시, IOException 발생하면, 예외 로그가 발생한다.")
    void webSocketSessionManager7() throws IOException {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        webSocketSessionManager.storeSession(1L, mockSession);
        webSocketSessionManager.storeSession(2L, mock(WebSocketSession.class));

        doThrow(new IOException("test exception")).when(mockSession).close();

        webSocketSessionManager.terminateSession(1L);

        verify(mockSession, times(1)).close();

        WebSocketSession closedSession = webSocketSessionManager.findSessionByUserId(1L);
        assertThat(closedSession).isNull();
    }

    @Test
    @DisplayName("메시지가 정상적으로 보내진다.")
    void webSocketSessionManager8() throws IOException {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        webSocketSessionManager.storeSession(1L, mockSession);

        MessageResponse baseResponse = new MessageResponse(1L, "inviter", "message");
        webSocketSessionManager.sendMessage(mockSession, baseResponse.getContent());

        verify(mockSession, times(1))
                .sendMessage(new TextMessage(baseResponse.getContent()));
    }

    @Test
    @DisplayName("메시지를 전송 중 예외가 발생하면, 메시지가 보내지지 않는다.")
    void webSocketSessionManager8_1() throws IOException {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        webSocketSessionManager.storeSession(1L, mockSession);

        MessageResponse baseResponse = new MessageResponse(1L, "inviter", "message");

        doThrow(new IOException("test exception"))
                .when(mockSession)
                .sendMessage(any());

        LogCaptor logCaptor = LogCaptor.forClass(WebSocketSessionManager.class);

        assertThatThrownBy(() -> webSocketSessionManager.sendMessage(mockSession, baseResponse.getContent()))
                .isInstanceOf(IOException.class);

        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));

        assertThat(logCaptor.getErrorLogs()).anyMatch(log -> log.contains("[sendMessage] Failed to send TextMessage:"));

        logCaptor.close();
    }
}