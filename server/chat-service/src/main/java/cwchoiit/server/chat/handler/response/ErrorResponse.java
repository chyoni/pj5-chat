package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.ERROR;

@Getter
@ToString
public class ErrorResponse extends BaseResponse {
    private final String messageType;
    private final String message;

    public ErrorResponse(String messageType, String message) {
        super(ERROR);
        this.messageType = messageType;
        this.message = message;
    }
}
