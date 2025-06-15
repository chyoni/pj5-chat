package cwchoiit.chat.server.service.request.kafka.push.outbound;


import cwchoiit.chat.server.constants.UserConnectionStatus;

import static cwchoiit.chat.server.constants.MessageType.INVITE_RESPONSE;

public record InviteOutboundMessage(Long userId,
                                    String connectionInviteCode,
                                    UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return INVITE_RESPONSE;
    }
}
