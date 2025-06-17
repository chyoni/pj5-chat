package cwchoiit.server.chat.handler.response;

import cwchoiit.server.chat.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.INVITE_RESPONSE;

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
