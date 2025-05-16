package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.Constants;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.DisconnectRequest;
import cwchoiit.chat.server.handler.response.AcceptNotificationResponse;
import cwchoiit.chat.server.handler.response.AcceptResponse;
import cwchoiit.chat.server.handler.response.DisconnectResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.UserConnectionService;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.DISCONNECT_REQUEST;
import static cwchoiit.chat.server.constants.UserConnectionStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public String messageType() {
        return DISCONNECT_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof DisconnectRequest disconnectRequest) {
            Long requestUserId = (Long) session.getAttributes().get(Constants.USER_ID.getValue());
            Pair<Boolean, String> result = userConnectionService.disconnect(
                    requestUserId,
                    disconnectRequest.getPeerUsername()
            );

            if (result.getFirst()) {
                sessionManager.sendMessage(
                        session,
                        new DisconnectResponse(disconnectRequest.getPeerUsername(), DISCONNECTED)
                );
            } else {
                String errorMessage = result.getSecond();
                sessionManager.sendMessage(
                        session,
                        new ErrorResponse(DISCONNECT_REQUEST, errorMessage)
                );
            }
        }
    }
}
