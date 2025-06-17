package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.ACCEPT_RESPONSE;

public record AcceptReceiveMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return ACCEPT_RESPONSE;
    }
}
