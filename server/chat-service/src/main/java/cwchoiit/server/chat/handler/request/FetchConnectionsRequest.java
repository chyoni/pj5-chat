package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CONNECTIONS_REQUEST;

@Getter
public class FetchConnectionsRequest extends BaseRequest {

    private final UserConnectionStatus status;

    @JsonCreator
    public FetchConnectionsRequest(@JsonProperty("status") UserConnectionStatus status) {
        super(FETCH_CONNECTIONS_REQUEST);
        this.status = status;
    }
}
