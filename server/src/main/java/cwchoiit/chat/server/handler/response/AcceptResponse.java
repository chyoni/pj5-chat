package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_RESPONSE;

@Getter
@ToString
public class AcceptResponse extends BaseResponse {
    private final String username;

    public AcceptResponse(String username) {
        super(ACCEPT_RESPONSE);
        this.username = username;
    }
}
