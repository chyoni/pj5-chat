package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.ENTER_CHANNEL_REQUEST;

@Getter
public class EnterChannelSendMessage extends BaseSendMessage {

    private final Long channelId;

    public EnterChannelSendMessage(Long channelId) {
        super(ENTER_CHANNEL_REQUEST);
        this.channelId = channelId;
    }
}
