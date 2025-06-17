package cwchoiit.server.chat.handler;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.adapter.RequestHandlerDispatcher;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final RequestHandlerDispatcher requestHandlerDispatcher;

    /**
     * Handles the establishment of a new WebSocket connection. Logs session details
     * and decorates the session with concurrent handling capabilities for better
     * performance and resource management. The decorated session is then stored for
     * future use.
     * <p>
     * 한가지 더 추가적으로, {@link ConcurrentWebSocketSessionDecorator} 이 녀석을 사용하는 이유는
     * WebSocketSession을 Thread-safe 하게 사용하기 위함이다. 기본적으로 WebSocketSession은 Thread-safe 하지 않다.
     * <p>
     * WebSocketSession.sendMessage()를 호출하면 내부적으로 버퍼와 네트워크 스트림에 쓰기 작업(write)을 하는데,
     * 여러 스레드가 동시에 write 작업을 하면 race condition이 발생한다. 그래서, 메시지가 섞여서 전송되거나, 이미 쓰고 있는 중인데
     * 또 쓰려니까 예외가 터지거나, 버퍼에 손상이 생긴다.
     * <p>
     * 이런 문제를 방지하기 위해, {@link ConcurrentWebSocketSessionDecorator} 이 래퍼를 사용하면 된다.
     * 얘는 내부적으로 {@link java.util.concurrent.BlockingQueue}에 메시지를 쌓아두고, 한 스레드가 순차적으로 꺼내서 sendMessage() 하도록
     * 직렬화한다.
     *
     * @param session the newly established WebSocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[afterConnectionEstablished] session id: {}", session.getId());

        int SEND_TIME_LIMIT = 5000; // 5초 이상 걸리는 메시지는 타임아웃 처리
        int BUFFERED_SIZE_LIMIT = 128 * 1024; // 최대 128KB
        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFERED_SIZE_LIMIT);

        sessionManager.storeSession(findUserIdBySession(session), concurrentWebSocketSessionDecorator);
    }

    /**
     * Handles transport errors that occur during a WebSocket session.
     * Logs the error and terminates the affected session to manage resources and ensure stability.
     *
     * @param session   the WebSocket session during which the error occurred
     * @param exception the error that occurred during the session
     */
    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        log.error("[handleTransportError] [{}] session id: {}", exception.getMessage(), session.getId(), exception);
        sessionManager.terminateSession(findUserIdBySession(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        log.info("[afterConnectionClosed] [{}] session id: {}", status, session.getId());
        sessionManager.terminateSession(findUserIdBySession(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("[handleTextMessage] Received TextMessage: [{}] from {}", message.getPayload(), session.getId());
        BaseRequest request = Serializer.deserialize(message.getPayload(), BaseRequest.class).orElseThrow();

        requestHandlerDispatcher.findHandler(request.getType())
                .ifPresent(handler -> handler.handle(request, session));
    }

    private Long findUserIdBySession(WebSocketSession session) {
        return (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
    }
}
