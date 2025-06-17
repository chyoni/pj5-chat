package cwchoiit.server.chat.service.request.kafka.push.outbound;

import static cwchoiit.server.chat.constants.MessageType.NOTIFY_ACCEPT;

public record AcceptNotificationOutboundMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_ACCEPT;
    }
}
