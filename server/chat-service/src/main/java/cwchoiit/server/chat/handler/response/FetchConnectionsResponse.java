package cwchoiit.server.chat.handler.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import cwchoiit.server.chat.service.response.ConnectionReadResponse;
import lombok.Getter;

import java.util.List;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CONNECTIONS_RESPONSE;

@Getter
public class FetchConnectionsResponse extends BaseResponse {

    private final List<ConnectionReadResponse> connections;

    @JsonCreator
    public FetchConnectionsResponse(List<ConnectionReadResponse> connections) {
        super(FETCH_CONNECTIONS_RESPONSE);
        this.connections = connections;
    }
}
