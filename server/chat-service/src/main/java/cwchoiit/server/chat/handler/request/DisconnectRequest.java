package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.DISCONNECT_REQUEST;

@Getter
public class DisconnectRequest extends BaseRequest {

    private final String peerUsername;

    @JsonCreator
    public DisconnectRequest(@JsonProperty("peerUsername") String peerUsername) {
        super(DISCONNECT_REQUEST);
        this.peerUsername = peerUsername;
    }
}
