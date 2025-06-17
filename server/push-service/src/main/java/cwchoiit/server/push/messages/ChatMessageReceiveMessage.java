package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.MESSAGE;


public record ChatMessageReceiveMessage(Long userId, String username, String content) implements BaseRecord {
    @Override
    public String type() {
        return MESSAGE;
    }
}
