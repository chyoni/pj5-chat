package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.MessageType;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.InviteRequest;
import cwchoiit.chat.server.handler.request.KeepAliveRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

import static cwchoiit.chat.server.constants.IdKey.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Adapter - MessageRequestHandler")
class MessageRequestHandlerTest extends SpringBootTestConfiguration {

    @Mock
    MessageService messageService;
    @InjectMocks
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
        messageRequestHandler.handle(new AcceptRequest("inviter"), mock(WebSocketSession.class));

        verify(messageService, never()).sendMessage(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("MessageRequest가 들어오면, 로직이 수행된다.")
    void handle_success() {
        WebSocketSession mock = mock(WebSocketSession.class);
        Map<String, Object> attributes = mock.getAttributes();
        attributes.put(USER_ID.getValue(), 1L);
        when(mock.getAttributes()).thenReturn(attributes);

        messageRequestHandler.handle(new MessageRequest(1L, "test", "test"), mock);

        verify(messageService, times(1))
                .sendMessage(eq(1L), eq(1L), eq("test"));
    }
}