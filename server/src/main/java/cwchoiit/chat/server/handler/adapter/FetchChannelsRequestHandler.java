package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.FetchChannelsRequest;
import cwchoiit.chat.server.handler.request.FetchConnectionsRequest;
import cwchoiit.chat.server.handler.response.FetchChannelsResponse;
import cwchoiit.chat.server.handler.response.FetchConnectionsResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.UserConnectionService;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import cwchoiit.chat.server.service.response.ConnectionReadResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNELS_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.FETCH_CONNECTIONS_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public String messageType() {
        return FETCH_CHANNELS_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof FetchChannelsRequest ignored) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            sessionManager.sendMessage(
                    session,
                    new FetchChannelsResponse(channelService.findChannelsByUserId(requestUserId))
            );
        }
    }
}
