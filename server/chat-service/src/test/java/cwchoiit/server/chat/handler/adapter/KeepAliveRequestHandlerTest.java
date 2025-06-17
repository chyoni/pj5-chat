package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.AcceptRequest;
import cwchoiit.server.chat.handler.request.InviteRequest;
import cwchoiit.server.chat.handler.request.KeepAliveRequest;
import cwchoiit.server.chat.handler.request.MessageRequest;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static cwchoiit.server.chat.constants.MessageType.KEEP_ALIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - KeepRequestHandler")
class KeepAliveRequestHandlerTest {

    @Mock
    SessionService sessionService;
    @Mock
    ChannelService channelService;
    @InjectMocks
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
        keepAliveRequestHandler.handle(new MessageRequest(1L, "test"), mock(WebSocketSession.class));
        keepAliveRequestHandler.handle(new InviteRequest(""), mock(WebSocketSession.class));
        keepAliveRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));

        verify(sessionService, never()).refreshTimeToLive(any());
    }

    @Test
    @DisplayName("KeepAliveRequest가 들어오면, 로직이 수행된다.")
    void handle_success() {
        WebSocketSession mocked = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(IdKey.HTTP_SESSION_ID.getValue(), "123");
        attributes.put(IdKey.USER_ID.getValue(), 1L);

        when(mocked.getAttributes()).thenReturn(attributes);
        keepAliveRequestHandler.handle(new KeepAliveRequest(), mocked);

        verify(sessionService, times(1)).refreshTimeToLive(eq("123"));
        verify(channelService, times(1)).refreshActiveChannel(eq(1L));
        verify(mocked, times(2)).getAttributes();
    }
}