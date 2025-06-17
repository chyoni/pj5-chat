package cwchoiit.server.chat.service.request.kafka.push.outbound;

import static cwchoiit.server.chat.constants.MessageType.ACCEPT_RESPONSE;

public record AcceptOutboundMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return ACCEPT_RESPONSE;
    }
}
