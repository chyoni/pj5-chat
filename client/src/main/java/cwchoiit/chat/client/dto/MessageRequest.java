package cwchoiit.chat.client.dto;

import cwchoiit.chat.client.constants.MessageType;
import lombok.Getter;

@Getter
public class MessageRequest extends BaseRequest {
    private String username;
    private String content;

    public MessageRequest() {
        super(MessageType.MESSAGE);
    }

    public MessageRequest(String username, String content) {
        super(MessageType.MESSAGE);
        this.username = username;
        this.content = content;
    }
}
