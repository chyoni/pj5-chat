package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.CHANNEL_CREATE_REQUEST;

@Getter
@ToString
public class CreateChannelSendMessage extends BaseSendMessage {

    private final String title;
    private final String participantUsername;

    public CreateChannelSendMessage(String title, String participantUsername) {
        super(CHANNEL_CREATE_REQUEST);
        this.title = title;
        this.participantUsername = participantUsername;
    }
}
