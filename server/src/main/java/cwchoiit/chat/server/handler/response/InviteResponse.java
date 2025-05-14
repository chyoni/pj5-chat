package cwchoiit.chat.server.handler.response;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.INVITE_RESPONSE;

@Getter
@ToString
public class InviteResponse extends BaseResponse {
    private final String connectionInviteCode;
    private final UserConnectionStatus status;

    public InviteResponse(String connectionInviteCode, UserConnectionStatus status) {
        super(INVITE_RESPONSE);
        this.connectionInviteCode = connectionInviteCode;
        this.status = status;
    }
}
