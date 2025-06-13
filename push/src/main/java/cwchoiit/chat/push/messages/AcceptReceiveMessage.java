package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.ACCEPT_RESPONSE;

public record AcceptReceiveMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return ACCEPT_RESPONSE;
    }
}
