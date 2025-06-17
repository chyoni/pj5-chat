package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.JOIN_CHANNEL_REQUEST;

@Getter
public class JoinChannelRequest extends BaseRequest {

    private final String inviteCode;

    @JsonCreator
    public JoinChannelRequest(@JsonProperty("inviteCode") String inviteCode) {
        super(JOIN_CHANNEL_REQUEST);
        this.inviteCode = inviteCode;
    }
}
