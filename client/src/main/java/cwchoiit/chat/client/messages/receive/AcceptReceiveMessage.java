package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.ACCEPT_RESPONSE;

@Getter
@ToString
public class AcceptReceiveMessage extends BaseReceiveMessage {
    private final String username;

    @JsonCreator
    public AcceptReceiveMessage(@JsonProperty("username") String username) {
        super(ACCEPT_RESPONSE);
        this.username = username;
    }
}
