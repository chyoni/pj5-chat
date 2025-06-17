package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.REJECT_REQUEST;

@Getter
public class RejectRequest extends BaseRequest {

    private final String inviterUsername;

    @JsonCreator
    public RejectRequest(@JsonProperty("inviterUsername") String inviterUsername) {
        super(REJECT_REQUEST);
        this.inviterUsername = inviterUsername;
    }
}
