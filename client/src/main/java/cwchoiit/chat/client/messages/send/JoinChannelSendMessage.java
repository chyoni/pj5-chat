package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.JOIN_CHANNEL_REQUEST;

@Getter
public class JoinChannelSendMessage extends BaseSendMessage {

    private final String inviteCode;

    public JoinChannelSendMessage(String inviteCode) {
        super(JOIN_CHANNEL_REQUEST);
        this.inviteCode = inviteCode;
    }
}
