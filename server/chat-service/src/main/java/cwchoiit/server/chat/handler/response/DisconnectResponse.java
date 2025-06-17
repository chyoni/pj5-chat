package cwchoiit.server.chat.handler.response;

import cwchoiit.server.chat.constants.UserConnectionStatus;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.DISCONNECT_RESPONSE;

@Getter
@ToString
public class DisconnectResponse extends BaseResponse {
    private final String username;
    private final UserConnectionStatus status;

    public DisconnectResponse(String username, UserConnectionStatus status) {
        super(DISCONNECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
