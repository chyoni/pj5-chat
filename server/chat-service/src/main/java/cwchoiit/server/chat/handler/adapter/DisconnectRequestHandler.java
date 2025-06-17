package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.DisconnectRequest;
import cwchoiit.server.chat.handler.response.DisconnectResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.DISCONNECT_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.DISCONNECTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return DISCONNECT_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof DisconnectRequest disconnectRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            Pair<Boolean, String> result = userConnectionService.disconnect(
                    requestUserId,
                    disconnectRequest.getPeerUsername()
            );

            if (result.getFirst()) {
                clientNotificationService.sendMessage(
                        session,
                        requestUserId,
                        new DisconnectResponse(disconnectRequest.getPeerUsername(), DISCONNECTED)
                );
            } else {
                String errorMessage = result.getSecond();
                clientNotificationService.sendMessage(
                        session,
                        requestUserId,
                        new ErrorResponse(DISCONNECT_REQUEST, errorMessage)
                );
            }
        }
    }
}
