package cwchoiit.chat.server.config;

import cwchoiit.chat.server.auth.WebSocketHttpSessionHandshakeInterceptor;
import cwchoiit.chat.server.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final MessageHandler messageHandler;
    private final WebSocketHttpSessionHandshakeInterceptor webSocketHttpSessionHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/ws/v1/message")
                .addInterceptors(webSocketHttpSessionHandshakeInterceptor);
    }
}
