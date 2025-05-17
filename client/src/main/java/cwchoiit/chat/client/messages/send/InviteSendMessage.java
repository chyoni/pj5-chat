package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.INVITE_REQUEST;

@Getter
@ToString
public class InviteSendMessage extends BaseSendMessage {

    private final String connectionInviteCode;

    public InviteSendMessage(String connectionInviteCode) {
        super(INVITE_REQUEST);
        this.connectionInviteCode = connectionInviteCode;
    }
}
