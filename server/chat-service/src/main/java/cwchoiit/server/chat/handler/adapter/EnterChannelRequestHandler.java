package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.ChannelResponse;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.EnterChannelRequest;
import cwchoiit.server.chat.handler.response.EnterChannelResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.ENTER_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return ENTER_CHANNEL_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof EnterChannelRequest enterChannelRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            Pair<Optional<String>, ChannelResponse> result = channelService.enter(
                    requestUserId,
                    enterChannelRequest.getChannelId()
            );

            result.getFirst().ifPresentOrElse(
                    title -> clientNotificationService.sendMessage(
                            session,
                            requestUserId,
                            new EnterChannelResponse(enterChannelRequest.getChannelId(), title)
                    ),
                    () -> clientNotificationService.sendMessage(
                            session,
                            requestUserId,
                            new ErrorResponse(ENTER_CHANNEL_REQUEST, result.getSecond().getMessage())
                    )
            );
        }
    }
}
