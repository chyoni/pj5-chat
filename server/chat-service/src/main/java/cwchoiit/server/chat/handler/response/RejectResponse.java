package cwchoiit.server.chat.handler.response;

import cwchoiit.server.chat.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.REJECT_RESPONSE;

@Getter
@ToString
public class RejectResponse extends BaseResponse {

    private final String username;
    private final UserConnectionStatus status;

    public RejectResponse(String username, UserConnectionStatus status) {
        super(REJECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
