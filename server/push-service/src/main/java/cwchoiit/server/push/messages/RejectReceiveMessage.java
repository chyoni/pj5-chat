package cwchoiit.server.push.messages;

import cwchoiit.server.push.constants.UserConnectionStatus;

import static cwchoiit.server.push.constants.MessageType.REJECT_RESPONSE;


public record RejectReceiveMessage(Long userId, String username, UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return REJECT_RESPONSE;
    }
}
