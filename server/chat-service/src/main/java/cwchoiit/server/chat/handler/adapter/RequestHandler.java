package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.handler.request.BaseRequest;
import org.springframework.web.socket.WebSocketSession;

public interface RequestHandler {
    String messageType();

    void handle(BaseRequest request, WebSocketSession session);
}
