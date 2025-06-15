package cwchoiit.chat.server.service.request.kafka.push.outbound;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_RESPONSE;

public record AcceptOutboundMessage(Long userId, String username) implements BaseRecord {
    @Override
    public String type() {
        return ACCEPT_RESPONSE;
    }
}
