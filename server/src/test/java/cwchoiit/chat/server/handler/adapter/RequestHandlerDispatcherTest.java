package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.MessageType;
import cwchoiit.chat.server.handler.request.FetchUserInviteCodeRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Handler Adapter - RequestHandlerDispatcher")
class RequestHandlerDispatcherTest extends SpringBootTestConfiguration {

    @Autowired
    RequestHandlerDispatcher requestHandlerDispatcher;

    @Test
    @DisplayName("각 메시지 타입에 맞는 핸들러가 반환된다.")
    void findHandler() {
        RequestHandler requestHandler = requestHandlerDispatcher.findHandler(MessageType.MESSAGE).orElseThrow();
        assertThat(requestHandler).isNotNull();
        assertThat(requestHandler).isInstanceOf(MessageRequestHandler.class);

        RequestHandler requestHandler2 = requestHandlerDispatcher.findHandler(MessageType.INVITE_REQUEST).orElseThrow();
        assertThat(requestHandler2).isNotNull();
        assertThat(requestHandler2).isInstanceOf(InviteRequestHandler.class);

        RequestHandler requestHandler3 = requestHandlerDispatcher.findHandler(MessageType.KEEP_ALIVE).orElseThrow();
        assertThat(requestHandler3).isNotNull();
        assertThat(requestHandler3).isInstanceOf(KeepAliveRequestHandler.class);

        RequestHandler requestHandler4 = requestHandlerDispatcher.findHandler(MessageType.ACCEPT_REQUEST).orElseThrow();
        assertThat(requestHandler4).isNotNull();
        assertThat(requestHandler4).isInstanceOf(AcceptRequestHandler.class);

        RequestHandler requestHandler5 = requestHandlerDispatcher.findHandler(MessageType.FETCH_CONNECTIONS_REQUEST).orElseThrow();
        assertThat(requestHandler5).isNotNull();
        assertThat(requestHandler5).isInstanceOf(FetchConnectionsRequestHandler.class);

        RequestHandler requestHandler6 = requestHandlerDispatcher.findHandler(MessageType.FETCH_USER_INVITE_CODE_REQUEST).orElseThrow();
        assertThat(requestHandler6).isNotNull();
        assertThat(requestHandler6).isInstanceOf(FetchUserInviteCodeRequestHandler.class);

        RequestHandler requestHandler7 = requestHandlerDispatcher.findHandler(MessageType.REJECT_REQUEST).orElseThrow();
        assertThat(requestHandler7).isNotNull();
        assertThat(requestHandler7).isInstanceOf(RejectRequestHandler.class);

        RequestHandler requestHandler8 = requestHandlerDispatcher.findHandler(MessageType.DISCONNECT_REQUEST).orElseThrow();
        assertThat(requestHandler8).isNotNull();
        assertThat(requestHandler8).isInstanceOf(DisconnectRequestHandler.class);
    }

    @Test
    @DisplayName("유효하지 않은 메시지 타입인 경우, 대응되는 핸들러는 없다.")
    void findHandler_invalid() {
        boolean empty = requestHandlerDispatcher.findHandler("invalid").isEmpty();
        assertThat(empty).isTrue();
    }
}