package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import cwchoiit.chat.client.service.response.ConnectionReadResponse;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CONNECTIONS_RESPONSE;

@Getter
@ToString
public class FetchConnectionsReceiveMessage extends BaseReceiveMessage {

    private final List<ConnectionReadResponse> connections;

    @JsonCreator
    public FetchConnectionsReceiveMessage(@JsonProperty("connections") List<ConnectionReadResponse> connections) {
        super(FETCH_CONNECTIONS_RESPONSE);
        this.connections = connections;
    }
}
