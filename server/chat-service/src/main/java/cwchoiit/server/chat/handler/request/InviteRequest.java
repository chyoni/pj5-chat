package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.INVITE_REQUEST;

@Getter
public class InviteRequest extends BaseRequest {

    private final String connectionInviteCode;

    @JsonCreator
    public InviteRequest(@JsonProperty("connectionInviteCode") String connectionInviteCode) {
        super(INVITE_REQUEST);
        this.connectionInviteCode = connectionInviteCode;
    }
}
