package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.handler.request.BaseRequest;
import org.springframework.web.socket.WebSocketSession;

public interface RequestHandler {
    String messageType();

    void handle(BaseRequest request, WebSocketSession session);
}
