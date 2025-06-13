package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.MESSAGE;


public record ChatMessageReceiveMessage(Long userId, String username, String content) implements BaseRecord {
    @Override
    public String type() {
        return MESSAGE;
    }
}
