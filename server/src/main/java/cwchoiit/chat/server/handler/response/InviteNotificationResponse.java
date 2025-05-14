package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ASK_INVITE;

@Getter
@ToString
public class InviteNotificationResponse extends BaseResponse {

    private final String username;

    public InviteNotificationResponse(String username) {
        super(ASK_INVITE);
        this.username = username;
    }
}
