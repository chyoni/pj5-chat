package cwchoiit.server.chat.service.request.kafka.push.outbound;


import static cwchoiit.server.chat.constants.MessageType.ASK_INVITE;

public record InviteNotificationOutboundMessage(Long userId, String username) implements BaseRecord {

    @Override
    public String type() {
        return ASK_INVITE;
    }
}
