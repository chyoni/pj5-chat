package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.NOTIFY_ACCEPT;

public record AcceptNotificationReceiveMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_ACCEPT;
    }
}
