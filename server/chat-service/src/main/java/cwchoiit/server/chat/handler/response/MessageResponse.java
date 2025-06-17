package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.MESSAGE;

@Getter
@ToString
public class MessageResponse extends BaseResponse {
    private final Long channelId;
    private final String username;
    private final String content;

    public MessageResponse(Long channelId, String username, String content) {
        super(MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
