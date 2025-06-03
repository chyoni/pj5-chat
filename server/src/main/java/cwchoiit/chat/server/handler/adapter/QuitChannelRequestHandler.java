package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.LeaveChannelRequest;
import cwchoiit.chat.server.handler.request.QuitChannelRequest;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.LeaveChannelResponse;
import cwchoiit.chat.server.handler.response.QuitChannelResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.ChannelResponse.*;
import static cwchoiit.chat.server.constants.MessageType.LEAVE_CHANNEL_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.QUIT_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuitChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public String messageType() {
        return QUIT_CHANNEL_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof QuitChannelRequest quitChannelRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            try {
                ChannelResponse quitResponse = channelService.quit(quitChannelRequest.getChannelId(), requestUserId);
                if (quitResponse == SUCCESS) {
                    sessionManager.sendMessage(session, new QuitChannelResponse(quitChannelRequest.getChannelId()));
                } else {
                    sessionManager.sendMessage(
                            session,
                            new ErrorResponse(QUIT_CHANNEL_REQUEST, quitResponse.getMessage())
                    );
                }
            } catch (Exception e) {
                sessionManager.sendMessage(session, new ErrorResponse(QUIT_CHANNEL_REQUEST, e.getMessage()));
            }
        }
    }
}
