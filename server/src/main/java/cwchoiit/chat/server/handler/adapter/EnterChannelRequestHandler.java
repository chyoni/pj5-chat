package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.EnterChannelRequest;
import cwchoiit.chat.server.handler.response.EnterChannelResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.ENTER_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;

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
                    title -> sessionManager.sendMessage(
                            session,
                            new EnterChannelResponse(enterChannelRequest.getChannelId(), title)
                    ),
                    () -> sessionManager.sendMessage(
                            session,
                            new ErrorResponse(ENTER_CHANNEL_REQUEST, result.getSecond().getMessage())
                    )
            );
        }
    }
}
