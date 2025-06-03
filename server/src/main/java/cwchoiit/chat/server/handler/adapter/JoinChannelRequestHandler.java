package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.FetchChannelsRequest;
import cwchoiit.chat.server.handler.request.JoinChannelRequest;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.FetchChannelsResponse;
import cwchoiit.chat.server.handler.response.JoinChannelResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNELS_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.JOIN_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public String messageType() {
        return JOIN_CHANNEL_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof JoinChannelRequest joinChannelRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            try {
                Pair<Optional<ChannelReadResponse>, ChannelResponse> response =
                        channelService.join(joinChannelRequest.getInviteCode(), requestUserId);

                response.getFirst().ifPresentOrElse(
                        channel -> sessionManager.sendMessage(
                                session,
                                new JoinChannelResponse(channel.channelId(), channel.title())
                        ),
                        () -> sessionManager.sendMessage(
                                session,
                                new ErrorResponse(JOIN_CHANNEL_REQUEST, response.getSecond().getMessage())
                        )
                );
            } catch (Exception e) {
                sessionManager.sendMessage(
                        session,
                        new ErrorResponse(JOIN_CHANNEL_REQUEST, e.getMessage())
                );
            }
        }
    }
}
