package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.LeaveChannelRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.LeaveChannelResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.LEAVE_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return LEAVE_CHANNEL_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof LeaveChannelRequest ignored) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            if (channelService.leave(requestUserId)) {
                clientNotificationService.sendMessage(session, requestUserId, new LeaveChannelResponse());
            } else {
                clientNotificationService.sendMessage(
                        session,
                        requestUserId,
                        new ErrorResponse(LEAVE_CHANNEL_REQUEST, "You are not in this channel.")
                );
            }
        }
    }
}
