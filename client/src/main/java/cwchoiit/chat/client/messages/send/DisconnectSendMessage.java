package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.DISCONNECT_REQUEST;

@Getter
@ToString
public class DisconnectSendMessage extends BaseSendMessage {

    private final String peerUsername;

    public DisconnectSendMessage(String peerUsername) {
        super(DISCONNECT_REQUEST);
        this.peerUsername = peerUsername;
    }
}
