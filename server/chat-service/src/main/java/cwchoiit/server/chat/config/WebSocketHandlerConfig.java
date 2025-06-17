package cwchoiit.server.chat.config;

import cwchoiit.server.chat.auth.WebSocketHttpSessionHandshakeInterceptor;
import cwchoiit.server.chat.handler.AppWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * {@link EnableWebSocket} 애노테이션을 달면, 스프링에 WebSocket 관련 기능을 활성화 시킨다.
 * 따라서, 내부적으로 {@link WebSocketHandlerRegistry} 가 활성화 된다.
 * <p>
 * {@link WebSocketConfigurer}를 구현 -> registerWebSocketHandlers() 에서 WebSocket 핸들러를 등록하는 역할을 한다.
 * <p>
 * 이제부터, `/ws/v1/message/`라는 경로로 WebSocket 요청이 들어오면 내가 등록한 {@link AppWebSocketHandler}가 요청을 처리하게 된다.
 * 저 {@link WebSocketHttpSessionHandshakeInterceptor} 인터셉터는 WebSocket 연결 전에 HTTP Handshake 과정에서 세션 정보를 가로채기 위해 사용한다.
 * <p>
 * 왜 세션정보를 가로채냐면, 일단 이 채팅 프로그램에는 로그인한 사용자만 사용할 수 있고, 로그인을 하면 로그인 시 세션이 저장되는데 해당 세션은 HTTP 요청에 대한 세션이기 때문에
 * WebSocket 연결이 된 이후에는 세션을 따로 관리해주지 않아 우리가 직접 관리해야 한다. 그래서 WebSocket 연결전에 HTTP Handshake 시 세션을 가로채서 저장하고
 * 관리하기 위함이다.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final AppWebSocketHandler appWebSocketHandler;
    private final WebSocketHttpSessionHandshakeInterceptor webSocketHttpSessionHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(appWebSocketHandler, "/ws/v1/message")
                .addInterceptors(webSocketHttpSessionHandshakeInterceptor);
    }
}
