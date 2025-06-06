package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CHANNELS_REQUEST;

@Getter
public class FetchChannelsSendMessage extends BaseSendMessage {

    public FetchChannelsSendMessage() {
        super(FETCH_CHANNELS_REQUEST);
    }
}
