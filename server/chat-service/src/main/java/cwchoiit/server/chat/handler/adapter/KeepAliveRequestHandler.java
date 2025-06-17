package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.KeepAliveRequest;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.KEEP_ALIVE;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements RequestHandler {

    private final SessionService sessionService;
    private final ChannelService channelService;

    @Override
    public String messageType() {
        return KEEP_ALIVE;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof KeepAliveRequest) {
            String callerSessionId = (String) session.getAttributes().get(IdKey.HTTP_SESSION_ID.getValue());
            Long callerUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            sessionService.refreshTimeToLive(callerSessionId);
            channelService.refreshActiveChannel(callerUserId);
        }
    }
}
