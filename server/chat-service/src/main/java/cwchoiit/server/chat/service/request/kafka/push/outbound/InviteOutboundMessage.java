package cwchoiit.server.chat.service.request.kafka.push.outbound;


import cwchoiit.server.chat.constants.UserConnectionStatus;

import static cwchoiit.server.chat.constants.MessageType.INVITE_RESPONSE;

public record InviteOutboundMessage(Long userId,
                                    String connectionInviteCode,
                                    UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return INVITE_RESPONSE;
    }
}
