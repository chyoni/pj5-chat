package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_RESPONSE;
import static cwchoiit.chat.server.constants.MessageType.NOTIFY_ACCEPT;

@Getter
@ToString
public class AcceptNotificationResponse extends BaseResponse {
    private final String username;

    public AcceptNotificationResponse(String username) {
        super(NOTIFY_ACCEPT);
        this.username = username;
    }
}
