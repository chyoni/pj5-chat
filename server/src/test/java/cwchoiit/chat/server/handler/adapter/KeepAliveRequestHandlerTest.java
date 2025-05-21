package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.InviteRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static cwchoiit.chat.server.constants.MessageType.KEEP_ALIVE;
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
        keepAliveRequestHandler.handle(new MessageRequest(1L, "test", "test"), mock(WebSocketSession.class));
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

        when(mocked.getAttributes()).thenReturn(attributes);
        keepAliveRequestHandler.handle(new KeepAliveRequest(), mocked);

        verify(sessionService, times(1)).refreshTimeToLive(any());
        verify(channelService, times(1)).refreshActiveChannel(anyLong());
        verify(mocked, times(1)).getAttributes();
    }
}