package cwchoiit.chat.server.handler;

import cwchoiit.chat.serializer.Serializer;
import cwchoiit.chat.server.constants.Constants;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.entity.Message;
import cwchoiit.chat.server.repository.MessageRepository;
import cwchoiit.chat.server.service.SessionService;
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

    private final WebSocketSessionManager sessionManager;
    private final SessionService sessionService;
    private final MessageRepository messageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
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
        BaseRequest request = Serializer.deserialize(message.getPayload(), BaseRequest.class).orElseThrow();

        if (request instanceof MessageRequest messageRequest) {
            messageRepository.save(Message.create(messageRequest.getUsername(), messageRequest.getContent()));
            sessionManager.getSessions().stream()
                    .filter(s -> !s.getId().equals(session.getId()))
                    .forEach(s -> sendMessage(s, messageRequest));
        }
        if (request instanceof KeepAliveRequest) {
            sessionService.refreshTimeToLive(
                    (String) session.getAttributes().get(Constants.HTTP_SESSION_ID.getValue())
            );
        }
    }

    private void sendMessage(WebSocketSession session, MessageRequest messageRequest) {
        Serializer.serialize(messageRequest)
                .ifPresent(serializedMessage -> proceedSendMessage(session, messageRequest, serializedMessage));
    }

    private void proceedSendMessage(WebSocketSession session, MessageRequest messageRequest, String serializedMessage) {
        try {
            session.sendMessage(new TextMessage(serializedMessage));
            log.info("[sendMessage] Sent TextMessage: [{}] to {}", messageRequest, session.getId());
        } catch (Exception e) {
            log.error("[sendMessage] Failed to send TextMessage: [{}] to {}", messageRequest, session.getId(), e);
        }
    }
}
