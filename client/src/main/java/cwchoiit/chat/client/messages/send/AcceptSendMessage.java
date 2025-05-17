package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.ACCEPT_REQUEST;

@Getter
@ToString
public class AcceptSendMessage extends BaseSendMessage {

    private final String inviterUsername;

    public AcceptSendMessage(String inviterUsername) {
        super(ACCEPT_REQUEST);
        this.inviterUsername = inviterUsername;
    }
}
