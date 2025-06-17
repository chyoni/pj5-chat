package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.ASK_INVITE;

@Getter
@ToString
public class InviteNotificationResponse extends BaseResponse {

    private final String username;

    public InviteNotificationResponse(String username) {
        super(ASK_INVITE);
        this.username = username;
    }
}
