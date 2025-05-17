package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.constants.UserConnectionStatus;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.REJECT_RESPONSE;

@Getter
@ToString
public class RejectReceiveMessage extends BaseReceiveMessage {
    private final String username;
    private final UserConnectionStatus status;

    @JsonCreator
    public RejectReceiveMessage(@JsonProperty("username") String username,
                                @JsonProperty("status") UserConnectionStatus status) {
        super(REJECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
