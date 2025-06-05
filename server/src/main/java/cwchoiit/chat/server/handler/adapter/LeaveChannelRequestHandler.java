package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.LeaveChannelRequest;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.LeaveChannelResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.MessageType.LEAVE_CHANNEL_REQUEST;

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
