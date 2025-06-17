package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.ChannelResponse;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.JoinChannelRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.JoinChannelResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.response.ChannelReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.JOIN_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

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
                        channel -> clientNotificationService.sendMessage(
                                session,
                                requestUserId,
                                new JoinChannelResponse(channel.channelId(), channel.title())
                        ),
                        () -> clientNotificationService.sendMessage(
                                session,
                                requestUserId,
                                new ErrorResponse(JOIN_CHANNEL_REQUEST, response.getSecond().getMessage())
                        )
                );
            } catch (Exception e) {
                clientNotificationService.sendMessage(
                        session,
                        requestUserId,
                        new ErrorResponse(JOIN_CHANNEL_REQUEST, e.getMessage())
                );
            }
        }
    }
}
