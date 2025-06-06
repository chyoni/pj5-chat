package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.FetchConnectionsRequest;
import cwchoiit.chat.server.handler.response.FetchConnectionsResponse;
import cwchoiit.chat.server.service.ClientNotificationService;
import cwchoiit.chat.server.service.UserConnectionService;
import cwchoiit.chat.server.service.response.ConnectionReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CONNECTIONS_REQUEST;

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
