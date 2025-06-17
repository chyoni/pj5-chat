package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.ACCEPT_RESPONSE;

@Getter
@ToString
public class AcceptResponse extends BaseResponse {
    private final String username;

    public AcceptResponse(String username) {
        super(ACCEPT_RESPONSE);
        this.username = username;
    }
}
