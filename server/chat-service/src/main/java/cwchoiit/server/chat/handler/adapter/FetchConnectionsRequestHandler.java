package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.FetchConnectionsRequest;
import cwchoiit.server.chat.handler.response.FetchConnectionsResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import cwchoiit.server.chat.service.response.ConnectionReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CONNECTIONS_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return FETCH_CONNECTIONS_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof FetchConnectionsRequest fetchConnectionsRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            List<ConnectionReadResponse> connections = userConnectionService.findConnectionUsersByStatus(
                            requestUserId,
                            fetchConnectionsRequest.getStatus())
                    .stream()
                    .map(response -> new ConnectionReadResponse(
                                    response.username(),
                                    fetchConnectionsRequest.getStatus()
                            )
                    ).toList();

            clientNotificationService.sendMessage(session, requestUserId, new FetchConnectionsResponse(connections));
        }
    }
}
