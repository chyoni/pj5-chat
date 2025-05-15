package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_REQUEST;

@Getter
public class AcceptRequest extends BaseRequest {

    private final String inviterUsername;

    @JsonCreator
    public AcceptRequest(@JsonProperty("inviterUsername") String inviterUsername) {
        super(ACCEPT_REQUEST);
        this.inviterUsername = inviterUsername;
    }
}
