package cwchoiit.chat.server.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 서버에 연결된 세션(그룹채팅에 접속한 유저들이라고 생각하면 편함)들을 관리하는 클래스
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public List<WebSocketSession> getSessions() {
        return List.copyOf(sessions.values());
    }

    public WebSocketSession findSessionByUserId(Long userId) {
        return sessions.get(userId);
    }

    public void storeSession(Long userId, WebSocketSession session) {
        log.info("[storeSession] user id: {}, session id: {}", userId, session.getId());
        sessions.put(userId, session);
    }

    public void terminateSession(Long userId) {
        log.info("[terminateSession] user id: {}", userId);
        WebSocketSession removedSession = sessions.remove(userId);
        try {
            if (removedSession != null) {
                removedSession.close();
                log.info("[terminateSession] Closed session: {}", userId);
            }
        } catch (IOException e) {
            log.error("[terminateSession] Failed to close session: {}", userId, e);
        }
    }

    public void sendMessage(WebSocketSession session, String message) throws IOException {
        try {
            session.sendMessage(new TextMessage(message));
            log.info("[sendMessage] Sent TextMessage: [{}] to {}", message, session.getId());
        } catch (IOException e) {
            log.error("[sendMessage] Failed to send TextMessage: [{}] to {}", message, session.getId(), e);
            throw e;
        }
    }
}
