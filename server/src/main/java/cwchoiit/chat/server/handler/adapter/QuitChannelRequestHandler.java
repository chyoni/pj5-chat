package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.QuitChannelRequest;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.QuitChannelResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.ChannelResponse.SUCCESS;
import static cwchoiit.chat.server.constants.MessageType.QUIT_CHANNEL_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuitChannelRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

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
                    clientNotificationService.sendMessage(
                            session,
                            requestUserId,
                            new QuitChannelResponse(quitChannelRequest.getChannelId())
                    );
                } else {
                    clientNotificationService.sendMessage(
                            session,
                            requestUserId,
                            new ErrorResponse(QUIT_CHANNEL_REQUEST, quitResponse.getMessage())
                    );
                }
            } catch (Exception e) {
                clientNotificationService.sendMessage(
                        session,
                        requestUserId,
                        new ErrorResponse(QUIT_CHANNEL_REQUEST, e.getMessage())
                );
            }
        }
    }
}
