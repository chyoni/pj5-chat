package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.NOTIFY_ACCEPT;

public record AcceptNotificationReceiveMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_ACCEPT;
    }
}
