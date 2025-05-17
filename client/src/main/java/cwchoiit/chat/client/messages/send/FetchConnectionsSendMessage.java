package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.constants.UserConnectionStatus;
import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CONNECTIONS_REQUEST;

@Getter
@ToString
public class FetchConnectionsSendMessage extends BaseSendMessage {

    private final UserConnectionStatus status;

    public FetchConnectionsSendMessage(UserConnectionStatus status) {
        super(FETCH_CONNECTIONS_REQUEST);
        this.status = status;
    }
}
