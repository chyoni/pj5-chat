package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.ASK_INVITE;

@Getter
@ToString
public class InviteNotificationReceiveMessage extends BaseReceiveMessage {

    private final String username;

    @JsonCreator
    public InviteNotificationReceiveMessage(@JsonProperty("username") String username) {
        super(ASK_INVITE);
        this.username = username;
    }
}
