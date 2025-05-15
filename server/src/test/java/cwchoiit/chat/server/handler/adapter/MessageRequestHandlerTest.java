package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.MessageType;
import cwchoiit.chat.server.entity.Message;
import cwchoiit.chat.server.handler.request.InviteRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.handler.response.MessageResponse;
import cwchoiit.chat.server.repository.MessageRepository;
import cwchoiit.chat.server.repository.UserRepository;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Handler Adapter - MessageRequestHandler")
class MessageRequestHandlerTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    MessageRepository messageRepository;

    @MockitoSpyBean
    WebSocketSessionManager sessionManager;

    @Autowired
    MessageRequestHandler messageRequestHandler;

    @Test
    @DisplayName("MessageRequestHandler는 MessageType이 [MESSAGE]일때 처리할 수 있다.")
    void isSupported() {
        String messageType = messageRequestHandler.messageType();
        assertThat(messageType).isEqualTo(MessageType.MESSAGE);
    }

    @Test
    @DisplayName("MessageRequestHandler는 BaseRequest 인스턴스 타입이 MessageRequest일 때 처리할 수 있다.")
    void handle() {
        messageRequestHandler.handle(new KeepAliveRequest(), mock(WebSocketSession.class));
        messageRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("MessageRequest가 들어오면, 로직이 수행된다.")
    void handle_success() {
        messageRequestHandler.handle(new MessageRequest("test", "test"), mock(WebSocketSession.class));

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(sessionManager, times(1)).getSessions();
    }

    @Test
    @DisplayName("MessageRequest가 들어오면, 저장되어 있는 세션들에게 (본인을 제외하고) 메시지를 보낸다.")
    void handle_success2() {
        WebSocketSession mockSession1 = mock(WebSocketSession.class);
        WebSocketSession mockSession2 = mock(WebSocketSession.class);

        when(mockSession1.getId()).thenReturn("session1");
        when(mockSession2.getId()).thenReturn("session2");

        sessionManager.storeSession(1L, mockSession1);
        sessionManager.storeSession(2L, mockSession2);

        messageRequestHandler.handle(new MessageRequest("test", "test"), mockSession1);

        verify(sessionManager, times(1)).getSessions();
        verify(sessionManager, times(1))
                .sendMessage(eq(mockSession2), any(MessageResponse.class));
        verify(sessionManager, never())
                .sendMessage(eq(mockSession1), any(MessageResponse.class));
    }
}