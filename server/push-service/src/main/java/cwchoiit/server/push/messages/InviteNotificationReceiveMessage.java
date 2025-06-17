package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.ASK_INVITE;

public record InviteNotificationReceiveMessage(Long userId, String username) implements BaseRecord {

    @Override
    public String type() {
        return ASK_INVITE;
    }
}
