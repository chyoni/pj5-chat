package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.NOTIFY_ACCEPT;

@Getter
@ToString
public class AcceptNotificationReceiveMessage extends BaseReceiveMessage {
    private final String username;

    @JsonCreator
    public AcceptNotificationReceiveMessage(@JsonProperty("username") String username) {
        super(NOTIFY_ACCEPT);
        this.username = username;
    }
}
