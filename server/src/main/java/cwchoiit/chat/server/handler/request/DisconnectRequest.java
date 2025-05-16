package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.DISCONNECT_REQUEST;

@Getter
public class DisconnectRequest extends BaseRequest {

    private final String peerUsername;

    @JsonCreator
    public DisconnectRequest(@JsonProperty("peerUsername") String peerUsername) {
        super(DISCONNECT_REQUEST);
        this.peerUsername = peerUsername;
    }
}
