package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.constants.UserConnectionStatus;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.INVITE_RESPONSE;

@Getter
@ToString
public class InviteReceiveMessage extends BaseReceiveMessage {
    private final String connectionInviteCode;
    private final UserConnectionStatus status;

    @JsonCreator
    public InviteReceiveMessage(@JsonProperty("connectionInviteCode") String connectionInviteCode,
                                @JsonProperty("status") UserConnectionStatus status) {
        super(INVITE_RESPONSE);
        this.connectionInviteCode = connectionInviteCode;
        this.status = status;
    }
}
