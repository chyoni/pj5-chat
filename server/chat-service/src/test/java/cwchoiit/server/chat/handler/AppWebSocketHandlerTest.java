package cwchoiit.server.chat.handler;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.MessageType;
import cwchoiit.server.chat.handler.adapter.RequestHandlerDispatcher;
import cwchoiit.server.chat.handler.request.MessageRequest;
import cwchoiit.server.chat.session.WebSocketSessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppWebSocketHandler")
class AppWebSocketHandlerTest {

    @Mock
    WebSocketSessionManager sessionManager;

    @Mock
    RequestHandlerDispatcher requestHandlerDispatcher;

    @InjectMocks
    AppWebSocketHandler appWebSocketHandler;

    @Test
    @DisplayName("afterConnectionEstablished 호출 시, 세션 매니저에 세션이 등록된다.")
    void afterConnectionEstablished() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getAttributes()).thenReturn(attributes);

        appWebSocketHandler.afterConnectionEstablished(mockSession);

        ArgumentCaptor<ConcurrentWebSocketSessionDecorator> captor = ArgumentCaptor.forClass(ConcurrentWebSocketSessionDecorator.class);

        verify(sessionManager, times(1))
                .storeSession(eq(1L), captor.capture());

        ConcurrentWebSocketSessionDecorator value = captor.getValue();
        assertThat(value.getBufferSizeLimit()).isEqualTo(128 * 1024);
        assertThat(value.getSendTimeLimit()).isEqualTo(5000L);
        assertThat(value.getLastSession()).isEqualTo(mockSession);
    }

    @Test
    @DisplayName("handleTransportError 호출 시, 세션 매니저에 해당 세션이 삭제된다.")
    void handleTransportError() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getAttributes()).thenReturn(attributes);

        appWebSocketHandler.handleTransportError(mockSession, new Exception("Network Error"));

        verify(sessionManager, times(1)).terminateSession(eq(1L));
    }

    @Test
    @DisplayName("afterConnectionClosed 호출 시, 세션 매니저에 해당 세션이 삭제된다.")
    void afterConnectionClosed() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getAttributes()).thenReturn(attributes);

        appWebSocketHandler.afterConnectionClosed(mockSession, CloseStatus.NORMAL);

        verify(sessionManager, times(1)).terminateSession(eq(1L));
    }

    @Test
    @DisplayName("handleTextMessage 호출 시, 핸들러 디스패처에 의해 적절한 핸들러를 찾아 메시지를 처리한다.")
    void handleTextMessage() {
        WebSocketSession mockSession = mock(WebSocketSession.class);

        String message = Serializer.serialize(new MessageRequest(1L, "message")).orElseThrow();
        TextMessage textMessage = new TextMessage(message);

        appWebSocketHandler.handleTextMessage(mockSession, textMessage);

        verify(requestHandlerDispatcher, times(1)).findHandler(eq(MessageType.MESSAGE));
    }
}