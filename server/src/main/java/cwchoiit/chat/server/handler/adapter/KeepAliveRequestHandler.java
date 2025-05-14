package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.Constants;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.MessageType.KEEP_ALIVE;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements RequestHandler {

    private final SessionService sessionService;

    @Override
    public boolean isSupported(String type) {
        return type.equals(KEEP_ALIVE);
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        sessionService.refreshTimeToLive((String) session.getAttributes().get(Constants.HTTP_SESSION_ID.getValue()));
    }
}
