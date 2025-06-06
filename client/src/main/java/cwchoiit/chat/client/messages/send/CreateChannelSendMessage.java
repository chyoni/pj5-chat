package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static cwchoiit.chat.client.constants.MessageType.CHANNEL_CREATE_REQUEST;

@Getter
@ToString
public class CreateChannelSendMessage extends BaseSendMessage {

    private final String title;
    private final List<String> participantUsernames;

    public CreateChannelSendMessage(String title, List<String> participantUsernames) {
        super(CHANNEL_CREATE_REQUEST);
        this.title = title;
        this.participantUsernames = participantUsernames;
    }
}
