package cwchoiit.chat.server.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public List<WebSocketSession> getSessions() {
        return List.copyOf(sessions.values());
    }

    public void storeSession(WebSocketSession session) {
        log.info("[storeSession] session id: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    public void terminateSession(String sessionId) {
        log.info("[terminateSession] session id: {}", sessionId);
        WebSocketSession removedSession = sessions.remove(sessionId);
        try {
            if (removedSession != null) {
                removedSession.close();
                log.info("[terminateSession] Closed session: {}", sessionId);
            }
        } catch (IOException e) {
            log.error("[terminateSession] Failed to close session: {}", sessionId, e);
        }
    }
}
