package cwchoiit.server.push.messages;

import cwchoiit.server.push.constants.UserConnectionStatus;

import static cwchoiit.server.push.constants.MessageType.INVITE_RESPONSE;


public record InviteReceiveMessage(Long userId,
                                   String connectionInviteCode,
                                   UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return INVITE_RESPONSE;
    }
}
