package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.MESSAGE;

@Getter
@ToString
public class ChatMessageReceiveMessage extends BaseReceiveMessage {
    private final String username;
    private final String content;

    @JsonCreator
    public ChatMessageReceiveMessage(@JsonProperty("username") String username,
                                     @JsonProperty("content") String content) {
        super(MESSAGE);
        this.username = username;
        this.content = content;
    }
}
