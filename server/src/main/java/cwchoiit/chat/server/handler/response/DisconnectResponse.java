package cwchoiit.chat.server.handler.response;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_RESPONSE;

@Getter
@ToString
public class DisconnectResponse extends BaseResponse {
    private final String username;
    private final UserConnectionStatus status;

    public DisconnectResponse(String username, UserConnectionStatus status) {
        super(ACCEPT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
