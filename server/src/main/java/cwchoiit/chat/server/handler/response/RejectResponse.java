package cwchoiit.chat.server.handler.response;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.REJECT_RESPONSE;

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
