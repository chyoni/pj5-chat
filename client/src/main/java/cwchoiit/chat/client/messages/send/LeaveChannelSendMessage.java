package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.LEAVE_CHANNEL_REQUEST;

@Getter
public class LeaveChannelSendMessage extends BaseSendMessage {

    public LeaveChannelSendMessage() {
        super(LEAVE_CHANNEL_REQUEST);
    }
}
