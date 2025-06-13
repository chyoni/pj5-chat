package cwchoiit.chat.push.messages;

import cwchoiit.chat.push.constants.UserConnectionStatus;

import static cwchoiit.chat.push.constants.MessageType.REJECT_RESPONSE;


public record RejectReceiveMessage(Long userId, String username, UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return REJECT_RESPONSE;
    }
}
