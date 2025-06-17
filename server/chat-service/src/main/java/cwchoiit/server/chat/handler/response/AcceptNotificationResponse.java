package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.NOTIFY_ACCEPT;

@Getter
@ToString
public class AcceptNotificationResponse extends BaseResponse {
    private final String username;

    public AcceptNotificationResponse(String username) {
        super(NOTIFY_ACCEPT);
        this.username = username;
    }
}
