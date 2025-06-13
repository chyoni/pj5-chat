package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.ASK_INVITE;

public record InviteNotificationReceiveMessage(Long userId, String username) implements BaseRecord {

    @Override
    public String type() {
        return ASK_INVITE;
    }
}
