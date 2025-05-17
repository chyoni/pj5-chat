package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.ERROR;

@Getter
@ToString
public class ErrorReceiveMessage extends BaseReceiveMessage {
    private final String messageType;
    private final String message;

    @JsonCreator
    public ErrorReceiveMessage(@JsonProperty("messageType") String messageType,
                               @JsonProperty("message") String message) {
        super(ERROR);
        this.messageType = messageType;
        this.message = message;
    }
}
