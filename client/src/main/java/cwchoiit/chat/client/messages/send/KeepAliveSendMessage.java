package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.KEEP_ALIVE;

@Getter
@ToString
public class KeepAliveSendMessage extends BaseSendMessage {

    public KeepAliveSendMessage() {
        super(KEEP_ALIVE);
    }
}
