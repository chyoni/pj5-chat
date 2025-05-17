package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.REJECT_REQUEST;

@Getter
@ToString
public class RejectSendMessage extends BaseSendMessage {

    private final String inviterUsername;

    public RejectSendMessage(String inviterUsername) {
        super(REJECT_REQUEST);
        this.inviterUsername = inviterUsername;
    }
}
