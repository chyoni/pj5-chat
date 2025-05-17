package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.constants.UserConnectionStatus;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.DISCONNECT_RESPONSE;

@Getter
@ToString
public class DisconnectReceiveMessage extends BaseReceiveMessage {
    private final String username;
    private final UserConnectionStatus status;

    @JsonCreator
    public DisconnectReceiveMessage(@JsonProperty("username") String username,
                                    @JsonProperty("status") UserConnectionStatus status) {
        super(DISCONNECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
