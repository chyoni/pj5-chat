package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.MessageType;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.InviteRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.service.SessionService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.MessageType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Handler Adapter - KeepRequestHandler")
class KeepAliveRequestHandlerTest extends SpringBootTestConfiguration {

    @MockitoBean
    SessionService sessionService;

    @Autowired
    KeepAliveRequestHandler keepAliveRequestHandler;

    @Test
    @DisplayName("KeepRequestHandler는 MessageType이 [KEEP_ALIVE]일때 처리할 수 있다.")
    void messageType() {
        String messageType = keepAliveRequestHandler.messageType();
        assertThat(messageType).isEqualTo(KEEP_ALIVE);
    }

    @Test
    @DisplayName("KeepRequestHandler는 BaseRequest 인스턴스 타입이 KeepAliveRequest일 때 처리할 수 있다.")
    void handle() {
        keepAliveRequestHandler.handle(new MessageRequest("test", "test"), mock(WebSocketSession.class));
        keepAliveRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));
        keepAliveRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));

        verify(sessionService, never()).refreshTimeToLive(any());
    }

    @Test
    @DisplayName("KeepAliveRequest가 들어오면, 로직이 수행된다.")
    void handle_success() {
        WebSocketSession mocked = mock(WebSocketSession.class);
        keepAliveRequestHandler.handle(new KeepAliveRequest(), mocked);

        verify(sessionService, times(1)).refreshTimeToLive(any());
        verify(mocked, times(1)).getAttributes();
    }
}