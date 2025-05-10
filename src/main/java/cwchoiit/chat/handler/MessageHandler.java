package cwchoiit.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cwchoiit.chat.dto.Message;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private WebSocketSession leftSide = null;
    private WebSocketSession rightSide = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[afterConnectionEstablished] session id: {}", session.getId());

        if (leftSide == null) {
            leftSide = session;
            return;
        } else if (rightSide == null) {
            rightSide = session;
            return;
        }

        log.warn("[afterConnectionEstablished] No available slots. Rejected session : {}", session.getId());
        session.close();
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("[handleTransportError] [{}] session id: {}", exception.getMessage(), session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        log.info("[afterConnectionClosed] [{}] session id: {}", status, session.getId());
        if (leftSide == session) {
            leftSide = null;
        } else if (rightSide == session) {
            rightSide = null;
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("[handleTextMessage] Received TextMessage: [{}] from {}", message.getPayload(), session.getId());
        String payload = message.getPayload();
        try {
            Message msg = objectMapper.readValue(payload, Message.class);
            if (leftSide == session) {
                sendMessage(rightSide, msg.content());
            } else if (rightSide == session) {
                sendMessage(leftSide, msg.content());
            }
        } catch (Exception e) {
            log.error("[handleTextMessage] Failed to parse TextMessage: [{}] from {}", payload, session.getId(), e);
            sendMessage(session, "Failed to parse message.");
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            String msg = objectMapper.writeValueAsString(new Message(message));
            session.sendMessage(new TextMessage(msg));
            log.info("[sendMessage] Sent TextMessage: [{}] to {}", msg, session.getId());
        } catch (Exception e) {
            log.error("[sendMessage] Failed to send TextMessage: [{}] to {}", message, session.getId(), e);
        }
    }
}
