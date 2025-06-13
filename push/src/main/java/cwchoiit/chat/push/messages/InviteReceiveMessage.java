package cwchoiit.chat.push.messages;

import cwchoiit.chat.push.constants.UserConnectionStatus;

import static cwchoiit.chat.push.constants.MessageType.INVITE_RESPONSE;


public record InviteReceiveMessage(Long userId,
                                   String connectionInviteCode,
                                   UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return INVITE_RESPONSE;
    }
}
