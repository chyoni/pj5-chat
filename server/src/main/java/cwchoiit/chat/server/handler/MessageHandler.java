package cwchoiit.chat.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cwchoiit.chat.server.dto.Message;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[afterConnectionEstablished] session id: {}", session.getId());

        int SEND_TIME_LIMIT = 5000;
        int BUFFERED_SIZE_LIMIT = 100 * 1024;
        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFERED_SIZE_LIMIT);

        sessionManager.storeSession(concurrentWebSocketSessionDecorator);
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        log.error("[handleTransportError] [{}] session id: {}", exception.getMessage(), session.getId(), exception);
        sessionManager.terminateSession(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        log.info("[afterConnectionClosed] [{}] session id: {}", status, session.getId());
        sessionManager.terminateSession(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("[handleTextMessage] Received TextMessage: [{}] from {}", message.getPayload(), session.getId());
        String payload = message.getPayload();
        try {
            Message receivedMessage = objectMapper.readValue(payload, Message.class);
            sessionManager.getSessions().stream()
                    .filter(s -> !s.getId().equals(session.getId()))
                    .forEach(s -> sendMessage(s, receivedMessage));
        } catch (Exception e) {
            log.error("[handleTextMessage] Failed to parse TextMessage: [{}] from {}", payload, session.getId(), e);
            sendMessage(session, new Message("System", "Failed to parse message"));
        }
    }

    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String msg = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(msg));
            log.info("[sendMessage] Sent TextMessage: [{}] to {}", msg, session.getId());
        } catch (Exception e) {
            log.error("[sendMessage] Failed to send TextMessage: [{}] to {}", message, session.getId(), e);
        }
    }
}
