package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.MESSAGE;

@Getter
@ToString
public class MessageResponse extends BaseResponse {
    private final String username;
    private final String content;

    public MessageResponse(String username, String content) {
        super(MESSAGE);
        this.username = username;
        this.content = content;
    }
}
