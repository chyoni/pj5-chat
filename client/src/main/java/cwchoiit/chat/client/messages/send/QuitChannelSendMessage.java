package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.QUIT_CHANNEL_REQUEST;

@Getter
public class QuitChannelSendMessage extends BaseSendMessage {

    private final Long channelId;

    public QuitChannelSendMessage(Long channelId) {
        super(QUIT_CHANNEL_REQUEST);
        this.channelId = channelId;
    }
}
