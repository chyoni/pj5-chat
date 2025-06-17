package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.FetchChannelsRequest;
import cwchoiit.server.chat.handler.response.FetchChannelsResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CHANNELS_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return FETCH_CHANNELS_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof FetchChannelsRequest ignored) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            clientNotificationService.sendMessage(
                    session,
                    requestUserId,
                    new FetchChannelsResponse(channelService.findChannelsByUserId(requestUserId))
            );
        }
    }
}
