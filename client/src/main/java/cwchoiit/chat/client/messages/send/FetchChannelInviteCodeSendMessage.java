package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST;

@Getter
public class FetchChannelInviteCodeSendMessage extends BaseSendMessage {

    private final Long channelId;

    public FetchChannelInviteCodeSendMessage(Long channelId) {
        super(FETCH_CHANNEL_INVITE_CODE_REQUEST);
        this.channelId = channelId;
    }
}
